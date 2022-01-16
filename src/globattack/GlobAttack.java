package globattack;

import java.util.ArrayList;

import processing.core.PApplet;
import processing.core.PImage;
import processing.core.PFont;

public class GlobAttack extends PApplet {
    float playerX = 256;
    float playerY = 352;
    boolean left, right, up, down;

    ArrayList<Enemy> enemies = new ArrayList<Enemy>();
    float enemySpeed = 1f;

    float bulletSpeed = 5;
    ArrayList<Bullet> bullets = new ArrayList<Bullet>();

    float spawnRate = 300;
    PImage backgroundImg;

    PImage[] playerAnim = new PImage[6];
    int animationFrame = 1;

    PImage[][] enemyAnimations = new PImage[3][6];

    PImage[] explosionAnimation = new PImage[6];

    int score = 0;
    int highScore = 0;
    PFont scoreFont;

    enum GameState {
        OVER, RUNNING
    }

    static GameState currentState;

    PImage gameOverImg;
    PImage restartButton;

    public static void main(String[] args) {
        PApplet.main("globattack.GlobAttack");
    }

    public void settings() {
        size(512, 704);
    }

    public void setup() {
        backgroundImg = loadImage("Images/Background.png");
        for (int i = 1; i <= 6; i++) {
            playerAnim[i - 1] = loadImage("Images/Bat_Brains_" + i + ".png");
            playerAnim[i - 1].resize(60, 0);
        }
        for (int j = 1; j <= 6; j++) {
            enemyAnimations[0][j - 1] = loadImage("Images/Bat_Purple" + j + ".png");
            enemyAnimations[1][j - 1] = loadImage("Images/Bat_Square" + j + ".png");
            enemyAnimations[2][j - 1] = loadImage("Images/Bat_Booger" + j + ".png");

            enemyAnimations[0][j - 1].resize(60, 0);
            enemyAnimations[1][j - 1].resize(60, 0);
            enemyAnimations[2][j - 1].resize(60, 0);
        }
        for (int i = 1; i <= 6; i++) {
            explosionAnimation[i - 1] = loadImage("Images/Explosion_FX" + i + ".png");
            explosionAnimation[i - 1].resize(60, 0);
        }
        currentState = GameState.RUNNING;
        gameOverImg = loadImage("Images/GameOverImg.png");
        gameOverImg.resize(300, 0);
        restartButton = loadImage("Images/WoodButton.png");
        restartButton.resize(240, 50);
    }

    public void draw() {
        drawBackground();
        switch (currentState) {
            case OVER:
                drawGameOver();
                break;

            case RUNNING:
                drawScore();
                noStroke();
                if (frameCount % 5 == 0) {
                    animationFrame++;
                    animationFrame = animationFrame % 6;
                    for (int i = 0; i < enemies.size(); i++) {
                        Enemy en = enemies.get(i);
                        if (en.isDead == true) {
                            en.explosionFrame++;
                            if (en.explosionFrame == 5) {
                                enemies.remove(i);
                            }
                        }
                    }
                }
                drawPlayer();
                increaseDifficulty();

                for (int b = 0; b < bullets.size(); b++) {
                    Bullet bull = bullets.get(b);
                    bull.move();
                    bull.drawBullet();
                    if (bull.x < 0 || bull.x > width || bull.y < 0 || bull.y > height) {
                        bullets.remove(b);
                    }
                }
                for (int i = 0; i < enemies.size(); i++) {
                    Enemy en = enemies.get(i);
                    en.move(playerX, playerY);
                    en.drawEnemy();
                    for (int j = 0; j < bullets.size(); j++) {
                        Bullet b = bullets.get(j);
                        if (abs(b.x - en.x) < 15 && abs(b.y - en.y) < 15 && en.isDead == false) {
                            en.isDead = true;
                            bullets.remove(j);
                            score += 1;
                            break;
                        }
                    }
                    if (abs(playerX - en.x) < 15 && abs(playerY - en.y) < 15) {
                        if (score > highScore) {
                            highScore = score;
                        }
                        currentState = GameState.OVER;
                    }
                }
                break;
        }

    }

    public void drawGameOver() {
        imageMode(CENTER);
        image(gameOverImg, width / 2, height / 2);
        fill(122, 64, 51);
        textAlign(CENTER);
        text("Game Over ", width / 2, height / 2 - 100);
        text("Score: " + score, width / 2, height / 2 - 40);
        text("High Score: " + highScore, width / 2, height / 2 + 10);
        image(restartButton, width / 2, height / 2 + 100);
        fill(255, 255, 255);
        text("Restart ", width / 2, height / 2 + 105);
    }

    public void drawScore() {
        scoreFont = createFont("Leelawadee UI Bold", 26, true);
        textFont(scoreFont);
        fill(255, 255, 255);
        textAlign(CENTER);
        text("Score: " + score, width - 90, 40);
    }

    public void drawBackground() {
        background(250);
        imageMode(CORNER);
        image(backgroundImg, 0, 0);
    }

    public void increaseDifficulty() {
        if (frameCount % spawnRate == 0) {
            generateEnemy();
            if (enemySpeed < 3) {
                enemySpeed += 0.1f;
            }
            if (spawnRate > 50) {
                spawnRate -= 10;
            }
        }
    }

    public void generateEnemy() {
        int side = (int) random(0, 2);
        int side2 = (int) random(0, 2);
        if (side % 2 == 0) { // top and bottom
            enemies.add(new Enemy(random(0, width), height * (side2 % 2), (int) random(0, 3)));
        } else { // sides
            enemies.add(new Enemy(width * (side2 % 2), random(0, height), (int) random(0, 3)));
        }
    }

    public void drawPlayer() {
        if (up) {
            playerY -= 5;
        }
        if (left) {
            playerX -= 5;
        }
        if (right) {
            playerX += 5;
        }
        if (down) {
            playerY += 5;
        }
        playerX = constrain(playerX, 70, width - 70);
        playerY = constrain(playerY, 70, height - 70);
        imageMode(CENTER);
        image(playerAnim[animationFrame], playerX, playerY);
    }

    public void mousePressed() {
        float dx = mouseX - playerX;
        float dy = mouseY - playerY;
        float angle = atan2(dy, dx);
        float vx = bulletSpeed * cos(angle);
        float vy = bulletSpeed * sin(angle);
        bullets.add(new Bullet(playerX, playerY, vx, vy));
    }

    public void keyPressed() {
        if (key == 'w') {
            up = true;
        }
        if (key == 'a') {
            left = true;
        }
        if (key == 's') {
            down = true;
        }
        if (key == 'd') {
            right = true;
        }
    }

    public void keyReleased() {
        if (key == 'w') {
            up = false;
        }
        if (key == 'a') {
            left = false;
        }
        if (key == 's') {
            down = false;
        }
        if (key == 'd') {
            right = false;
        }
    }

    class Enemy {
        float x, y, vx, vy;
        int enemyType = 0;
        boolean isDead = false;
        int explosionFrame = 0;

        Enemy(float x, float y, int enemyType) {
            this.x = x;
            this.y = y;
            this.enemyType = enemyType;
        }

        public void drawEnemy() {
            if (isDead == false) {
                imageMode(CENTER);
                image(enemyAnimations[enemyType][animationFrame], x, y);
            } else {
                image(explosionAnimation[explosionFrame], x, y);
            }
        }

        public void move(float px, float py) {
            if (isDead == false) {
                float angle = atan2(py - y, px - x);
                vx = cos(angle);
                vy = sin(angle);
                x += vx * enemySpeed;
                y += vy * enemySpeed;
            }
        }
    }

    class Bullet {
        float x, y, vx, vy;

        Bullet(float x, float y, float vx, float vy) {
            this.x = x;
            this.y = y;
            this.vx = vx;
            this.vy = vy;
        }

        void drawBullet() {
            fill(0, 255, 0);
            ellipse(x, y, 10, 10);
        }

        void move() {
            x += vx;
            y += vy;
        }
    }
}