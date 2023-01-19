package hr.fer.zemris.irg;

import java.util.ArrayList;
import java.util.List;

public class ParticleSystem {
    private final List<Particle> particleList;

    public ParticleSystem(int num) {
        particleList = new ArrayList<>();
        addParticles(num);
    }
    private void addParticles(int num) {
        for (int i = 0; i < num; i++) {
            Particle p = new Particle(new Point3D(320, 240, 0));
            particleList.add(p);
        }
    }

    public void update(double dt) {
        for (Particle p : particleList) {
            p.update(dt);
        }
        long time = System.currentTimeMillis();
        List<Particle> particlesToRemove = new ArrayList<>();
        for (Particle p : particleList) {
            if (p.position.getY() > 480 || p.timeOfDeletion <= time) {
                particlesToRemove.add(p);
            }
        }
        particleList.removeAll(particlesToRemove);
        addParticles(particlesToRemove.size());
    }

    public List<Particle> getParticleList() {
        return particleList;
    }
}
