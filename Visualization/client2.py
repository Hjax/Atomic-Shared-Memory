import pygame, sys, random, pexpect, threading, time, sys, math
from pexpect.popen_spawn import PopenSpawn

# Do some basic pygame init
pygame.init()
screen = pygame.display.set_mode((800, 600))

# load the image for the stick figure that represents the client
stick = pygame.transform.smoothscale(pygame.image.load("stick.png"), (64,64))

# a mutex to make sure multiple threads dont attempt to update the screen at the same time 
screen_lock = threading.Lock()

# a helper function that returns the distance between two points
def distance(a, b):
    return math.sqrt(math.pow(a[0] - b[0], 2) + math.pow(a[1] - b[1], 2))

# The class for communicating with the client jar
class API:
    def __init__(self):
        # spawn an instance of the jar
        self.child = PopenSpawn('java -jar client2.jar')
        # a mutex to ensure multiple requests dont happen at the same time
        self.lock = threading.Lock()
        self.lock.acquire()
        # log the interaction
        self.child.logfile = open("log.txt", "w")
        # run some startup commands 
        self.command('newserverset serversetAPI')
        port = random.randint(4000,5000)
        self.raw_command('managerport ' + str(port))
        self.raw_command('managerpcid ' + str(port))
        self.lock.release()
        
    # add a server to the APIs serverset
    def add_server(self, address, port):
        self.lock.acquire()
        self.command('newserver ' + address + ":" + str(port) + " " + address + " " + str(port))
        self.command('addserverset serversetAPI ' + address + ":" + str(port))
        self.lock.release()

    # call once after you set up the servers
    def start(self, port):
        self.lock.acquire()
        self.command('newclient clientAPI %d %d serversetAPI 810.0 810.0' % (port + 1, port + 1))
        self.raw_command('resend clientAPI 2000')
        self.lock.release()

    # runs a manager command (a command that doesnt give a response)
    def raw_command(self, command):
        sys.stdout.write(command + '\n')
        sys.stdout.flush()
        self.child.sendline(command)

    # runs a normal command (like a read or a write, that gives a response)
    def command(self, command):
        sys.stdout.write(command + '\n')
        sys.stdout.flush()
        self.child.sendline(command)
        self.child.readline()

    # an old version of command that is used because it also returns the value
    # from a read
    def read_old(self, command):
        sys.stdout.write(command + "\n")
        sys.stdout.flush()
        self.child.sendline(command)
        return self.child.readline().split("->")[-1].split("\n")[0]

    # perform a write 
    def write(self, key, value):
        self.command("write clientAPI " + str(key) + " " + str(value))

    # adjust a servers location (for simulated ping)
    def set_location(self, server, location):
        self.raw_command("setloc " + server + " " + str(float(location[0]) * 3) + " " + str(float(location[1]) * 3))

    # set the packet drop rate for a server
    def set_drop(self, server, rate):
        self.raw_command("drop " + str(int(rate * 100)) + " " +  server)

    # get the current value of the color key on a server and parse it into a tuple
    def get_color(self, server):
        self.lock.acquire()
        result = self.read_old('reliableread ' + server + ' color')
        sys.stdout.write("asdf: " + result + '\n')
        sys.stdout.flush()
        self.lock.release()
        if "null" in result:
            return (255, 255, 255)
        return (int(result[0:2], 16), int(result[2:4], 16), int(result[4:6], 16))

    # write a color to all of the server
    def set_color(self, color):
        self.command("write clientAPI color " + hex(color[0])[2:].zfill(2) + hex(color[1])[2:].zfill(2) + hex(color[2])[2:].zfill(2))

    # kill (disable) a server
    def kill(self, server):
        self.raw_command("kill " + server)

    # revive a server
    def revive(self, server):
        self.raw_command("revive " + server)

# draw the buttons to write colors to the servers
red = pygame.Rect((650, 50, 80, 50))
green = pygame.Rect((650, 250, 80, 50))
blue = pygame.Rect((650, 450, 80, 50))
colors = [red, green, blue]

# called repeatedly to draw the non moving parts of the frames
def draw_background():
    screen.fill((255, 255, 255))
    for i in range(9):
        pygame.draw.circle(screen, (0,0,0), (300, 300), (i + 1) * 30, 1)
    screen.blit(stick, (270, 270))
    pygame.draw.rect(screen, (255, 0, 0), red)
    pygame.draw.rect(screen, (0, 255, 0), green)
    pygame.draw.rect(screen, (0, 0, 255), blue)

# init pygames clock so we can limit FPS
clock = pygame.time.Clock()

# We need two API connections, one to spam reliablereads and one to do the regular (blocking) writes
connection = API()
reliable = API()

# a class for the server sprite in the UI
class Server(pygame.sprite.Sprite):
    def __init__(self):
        # load the image with the default color values
        pygame.sprite.Sprite.__init__(self)
        self.image = pygame.transform.smoothscale(pygame.image.load("server.png"), (64, 64))
        self.color = (255, 255, 255)
        self.rect = self.image.get_rect()
        self.following = False
        self.offset = (0, 0)
        self.address = None
        self.dead = False
        self.port = None

    # every frame if it sprite is following the mouse, update its location
    def update(self):
        if self.following:
            pos = pygame.mouse.get_pos()
            self.rect.center = (pos[0] + self.offset[0], pos[1] + self.offset[1])

    # used to determine if the user has clicked on a server sprite
    def collides_with_mouse(self):
        return self.rect.collidepoint(pygame.mouse.get_pos())

    # called when the user clicks a sprite
    def grab(self):
        pos = pygame.mouse.get_pos()
        self.offset = (self.rect.center[0] - pos[0], self.rect.center[1] - pos[1])
        self.following = True

    # called when the user releases the sprite
    def release(self):
        self.following = False
        self.update_location()

    # querys the api and updates the color
    def update_color(self):
        color = reliable.get_color(self.address + ":" + str(self.port))
        self.set_color(color)

    # used when the server is moved by the user and its ping needs to be adjusted
    def update_location(self):
        reliable.set_location(self.address + ":" + str(self.port), self.rect.center)

    # change the color of the sprite
    def set_color(self, color):
        screen_lock.acquire()
        pixels = pygame.PixelArray(self.image)
        pixels.replace(self.color, color)
        del pixels
        self.color = color
        screen_lock.release()

    # reloads the sprite with the version with the X when the server is killed and
    # reverts it when the server is revived, also preforms necessary API commands
    def toggle_death(self):
        if self.dead:
            reliable.revive(self.address + ":" + str(self.port))
            screen_lock.acquire()
            self.image = pygame.transform.smoothscale(pygame.image.load("server.png"), (64, 64))
            screen_lock.release()
            old_color = self.color
            self.color =  (255,255,255)
            self.set_color(old_color)
        else:
            reliable.kill(self.address + ":" + str(self.port))
            screen_lock.acquire()
            self.image = pygame.transform.smoothscale(pygame.image.load("server-dead.png"), (64, 64))
            screen_lock.release()
            old_color = self.color
            self.color =  (255,255,255)
            self.set_color(old_color)
        self.dead = not self.dead
        
# load the server addresses from the configuration file
server_addresses = open("servers.conf", "r").read().split('\n')
address_index = 0

# create the set of sprites for those addresses
servers = pygame.sprite.Group([Server() for x in range(len(server_addresses))])

# configure each of those sprites with the physical server they will be connecting to
for s in servers:
    s.rect.center = (random.randint(0, 600), random.randint(0, 600))
    s.address = server_addresses[address_index].split(":")[0]
    s.port = int(server_addresses[address_index].split(":")[1])
    connection.add_server(s.address, s.port)
    reliable.add_server(s.address, s.port)
    address_index += 1

# generate a random port to start on
port = random.randint(3000, 4000)
connection.start(port)
reliable.start(port + 2)

# a constant loop that updates the servers colors rapidly
def color_thread():
    while True:
        for s in servers:
            time.sleep(0.1)
            s.update_color()

# when the user does a write, this thread is spun up to prevent UI lock
def write_thread(color):
    try:
        connection.set_color(color)
    except:
        pass

# start the color update thread
t = threading.Thread(target=color_thread)
t.start()

# mainloop
while True:
    for event in pygame.event.get():
        # if they want to quit, quit
        if event.type == pygame.QUIT:
            pygame.quit()
            sys.exit()
        # if they click
        elif event.type == pygame.MOUSEBUTTONDOWN:
            # left click is for click and drag
            if event.button == 1:
                for s in reversed(servers.sprites()):
                    if s.collides_with_mouse():
                        s.grab()
                        break
                if red.collidepoint(pygame.mouse.get_pos()):
                    threading.Thread(target=write_thread, args=((255, 0, 0),)).start()
                if green.collidepoint(pygame.mouse.get_pos()):
                    threading.Thread(target=write_thread, args=((0, 255, 0),)).start()
                if blue.collidepoint(pygame.mouse.get_pos()):
                    threading.Thread(target=write_thread, args=((0, 0, 255),)).start()
            # right click is for disabling servers
            if event.button == 3:
                for s in reversed(servers.sprites()):
                    if s.collides_with_mouse():
                        s.toggle_death()
                        break
        # when they release the mouse, let go of click and drag
        elif event.type == pygame.MOUSEBUTTONUP:
            for s in servers:
                if s.following:
                    s.release()
    # redraw sprites and background and then wait at the frame limiter
    for s in servers:
        s.update()
    screen_lock.acquire()
    draw_background()
    servers.draw(screen)
    pygame.display.update()
    screen_lock.release()
    clock.tick(30)
