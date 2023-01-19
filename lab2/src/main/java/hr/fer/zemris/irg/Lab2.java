package hr.fer.zemris.irg;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.texture.Texture;
import com.jogamp.opengl.util.texture.TextureIO;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.jogamp.opengl.GL.*;

public class Lab2 extends JFrame {

    static {
        GLProfile.initSingleton();
    }

    private GLCanvas glCanvas;
    private final ParticleSystem system;
    private Texture texture;

    public Lab2() {

        system = new ParticleSystem(200);
        initGLCanvas();
        initListeners();
        initGUI();
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(task, 0, 1, TimeUnit.MILLISECONDS);

    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Lab2::new);
    }

    private void initGUI() {
        setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                dispose();
                System.exit(0);
            }
        });

        getContentPane().add(glCanvas, BorderLayout.CENTER);
        setSize(640, 480);
        setVisible(true);

        glCanvas.requestFocusInWindow();
    }

    private void initGLCanvas() {
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCanvas = new GLCanvas(glCapabilities);
    }

    private void initListeners() {
        glCanvas.addGLEventListener(new GLEventListener() {

            @Override
            public void init(GLAutoDrawable glAutoDrawable) {

            }

            @Override
            public void dispose(GLAutoDrawable glAutoDrawable) {

            }

            @Override
            public void display(GLAutoDrawable glAutoDrawable) {
                try {
                    if (texture == null) {
                        texture = loadTexture("balloon.bmp");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                GL2 gl2 = glAutoDrawable.getGL().getGL2();
                gl2.glClearColor(0, 0.3f, 0.6f, 0);
                gl2.glClear(GL_COLOR_BUFFER_BIT);
                gl2.glColor3d(1, 1, 1);
                gl2.glEnable(texture.getTarget());
                gl2.glBindTexture(texture.getTarget(), texture.getTextureObject());
                gl2.glEnable(GL_BLEND);
                gl2.glBlendFunc(GL_ONE, GL_ONE);
                gl2.glBegin(gl2.GL_QUADS);
                for (Particle p : system.getParticleList()) {
                    gl2.glTexCoord2d(0, 0);
                    gl2.glVertex3d(p.position.getX()-p.size, p.position.getY()-p.size, p.position.getZ());
                    gl2.glTexCoord2d(0, 1);
                    gl2.glVertex3d(p.position.getX()-p.size, p.position.getY()+p.size, p.position.getZ());
                    gl2.glTexCoord2d(1, 1);
                    gl2.glVertex3d(p.position.getX()+p.size, p.position.getY()+p.size, p.position.getZ());
                    gl2.glTexCoord2d(1, 0);
                    gl2.glVertex3d(p.position.getX()+p.size, p.position.getY()-p.size, p.position.getZ());
                }
                gl2.glEnd();
                gl2.glDisable(GL_BLEND);
                gl2.glDisable(texture.getTarget());
            }

            @Override
            public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int width, int height) {
                GL2 gl2 = glAutoDrawable.getGL().getGL2();
                gl2.glMatrixMode(GL2.GL_PROJECTION);
                gl2.glLoadIdentity();

                GLU glu = new GLU();
                glu.gluOrtho2D(0.0f, width, 0.0f, height);

                gl2.glMatrixMode(GL2.GL_MODELVIEW);
                gl2.glLoadIdentity();

                gl2.glViewport(0, 0, width, height);
            }
        });
    }

    public static Texture loadTexture(String file) throws GLException, IOException {
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(ImageIO.read(new File(file)), "png", os);
        InputStream fis = new ByteArrayInputStream(os.toByteArray());
        return TextureIO.newTexture(fis, true, TextureIO.PNG);
    }

    Runnable task = new Runnable() {
        public void run() {
            system.update(1./100);
            glCanvas.display();
        }
    };

}
