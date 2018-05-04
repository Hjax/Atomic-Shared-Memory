import pygame, sys, random, pexpect, threading, time, sys, math
from pexpect.popen_spawn import PopenSpawn

pygame.init()
screen = pygame.display.set_mode((800, 600))

done = False

stick = pygame.transform.smoothscale(pygame.image.load("stick.png"), (64,64))

screen_lock = threading.Lock()

def distance(a, b):
    return math.sqrt(math.pow(a[0] - b[0], 2) + math.pow(a[1] - b[1], 2))

# server names are just going to be their "address:port: string
class API:
    def __init__(self):
        self.child = PopenSpawn('java -jar client2.jar')
        self.lock = threading.Lock()
        self.lock.acquire()
        self.command('newserverset serversetAPI')
        self.lock.release()
        

    def add_server(self, address, port):
        self.lock.acquire()
        self.command('newserver ' + address + ":" + str(port) + " " + address + " " + str(port))
        self.command('addserverset serversetAPI ' + address + ":" + str(port))
        self.lock.release()

    def start(self, port):
        self.lock.acquire()
        self.raw_command('managerport ' + str(port))
        self.raw_command('managerpcid ' + str(port))
        self.command('newclient clientAPI 1 %d serversetAPI 270.0 270.0' % (port + 1))
        self.lock.release()

    def raw_command(self, command):
        sys.stdout.write(command + '\n')
        sys.stdout.flush()
        self.child.sendline(command)
    
    def command(self, command):
        sys.stdout.write(command + '\n')
        sys.stdout.flush()
        self.child.sendline(command)
        self.child.readline()
        #sys.stdout.write(self.child.readline())
        #sys.stdout.flush()

    def read(self, command):
        self.child.sendline(command)
        return self.child.readline().split("->")[-1].split("\n")[0]

    def get_color(self, server):
        self.lock.acquire()
        result = self.read('reliableread ' + server + ' color')
        self.lock.release()
        if "null" in result:
            return (255, 255, 255)
        return (int(result[0:2], 16), int(result[2:4], 16), int(result[4:6], 16))

    def set_color(self, color):
        self.command("write clientAPI color " + hex(color[0])[2:].zfill(2) + hex(color[1])[2:].zfill(2) + hex(color[2])[2:].zfill(2))

    def set_location(self, server, location):
        self.raw_command("setloc " + server + " " + str(float(location[0]) * 5) + " " + str(float(location[1])) * 5)

red = pygame.Rect((650, 50, 80, 50))
green = pygame.Rect((650, 250, 80, 50))
blue = pygame.Rect((650, 450, 80, 50))
colors = [red, green, blue]

def draw_background():
    screen.fill((255, 255, 255))
    for i in range(9):
        pygame.draw.circle(screen, (0,0,0), (300, 300), (i + 1) * 30, 1)
    screen.blit(stick, (270, 270))
    pygame.draw.rect(screen, (255, 0, 0), red)
    pygame.draw.rect(screen, (0, 255, 0), green)
    pygame.draw.rect(screen, (0, 0, 255), blue)

clock = pygame.time.Clock()
connection = API()
reliable = API()

class Server(pygame.sprite.Sprite):
    def __init__(self):
        pygame.sprite.Sprite.__init__(self)
        self.image = pygame.transform.smoothscale(pygame.image.load("server.png"), (64, 64))
        self.color = (255, 255, 255)
        self.rect = self.image.get_rect()
        self.following = False
        self.offset = (0, 0)
        self.address = None
        self.port = None

    def update(self):
        if self.following:
            pos = pygame.mouse.get_pos()
            self.rect.center = (pos[0] + self.offset[0], pos[1] + self.offset[1])

    def collides_with_mouse(self):
        return self.rect.collidepoint(pygame.mouse.get_pos())

    def grab(self):
        pos = pygame.mouse.get_pos()
        self.offset = (self.rect.center[0] - pos[0], self.rect.center[1] - pos[1])
        self.following = True

    def release(self):
        self.following = False
        self.update_location()

    def update_color(self):
        color = reliable.get_color(self.address + ":" + str(self.port))
        self.set_color(color)

    def update_location(self):
        reliable.set_location(self.address + ":" + str(self.port), self.rect.center)

    def set_color(self, color):
        pixels = pygame.PixelArray(self.image)
        pixels.replace(self.color, color)
        del pixels
        self.color = color
        

server_addresses = open("servers.conf", "r").read().split('\n')
address_index = 0

servers = pygame.sprite.Group([Server() for x in range(len(server_addresses))])


for s in servers:
    s.rect.center = (random.randint(0, 600), random.randint(0, 600))
    s.address = server_addresses[address_index].split(":")[0]
    s.port = int(server_addresses[address_index].split(":")[1])
    connection.add_server(s.address, s.port)
    reliable.add_server(s.address, s.port)
    address_index += 1

port = random.randint(3000, 4000)
connection.start(port)
reliable.start(port + 2)

def color_thread():
    while True:
        time.sleep(1)
        for s in servers:
            screen_lock.acquire()
            s.update_color()
            screen_lock.release()

t = threading.Thread(target=color_thread)
t.start()

while True:
    for event in pygame.event.get():
        if event.type == pygame.QUIT:
            pygame.quit()
            sys.exit()
        elif event.type == pygame.MOUSEBUTTONDOWN:
            for s in reversed(servers.sprites()):
                if s.collides_with_mouse():
                    s.grab()
                    break
            if red.collidepoint(pygame.mouse.get_pos()):
                connection.set_color((255, 0, 0))
            if green.collidepoint(pygame.mouse.get_pos()):
                connection.set_color((0, 255, 0))
            if blue.collidepoint(pygame.mouse.get_pos()):
                connection.set_color((0, 0, 255))
        elif event.type == pygame.MOUSEBUTTONUP:
            for s in servers:
                if s.following:
                    s.release()
    for s in servers:
        s.update()
    screen_lock.acquire()
    draw_background()
    servers.draw(screen)
    pygame.display.update()
    screen_lock.release()
    clock.tick(144)
