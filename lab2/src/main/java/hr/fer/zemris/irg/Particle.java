package hr.fer.zemris.irg;

import java.util.Random;

public class Particle {
    Point3D position;
    Random random;
    long timeOfDeletion;
    double size;

    public Particle(Point3D pos) {
        random = new Random();
        this.position = new Point3D(pos.getX() + (-320 + random.nextDouble() * 640),
                pos.getY() + (-240 + random.nextDouble() * 480), pos.getZ());
        this.timeOfDeletion = System.currentTimeMillis() + 10000;
        this.size = 20;
    }

    public void update(double dt) {
        position.setX(position.getX() + 10 * dt);
        position.setY(position.getY() + 50 * dt);
        size -= 10 * dt;
    }
}
