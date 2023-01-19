package hr.fer.zemris.irg;

import com.jogamp.opengl.*;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Zad5 extends JFrame {

    static {
        GLProfile.initSingleton();
    }

    private final int width = 640;
    private final int height = 480;
    private Point3D cameraPositon = new Point3D(1.0, 1.0, 1.3);
    private Point3D lookAt = new Point3D(0.0, 0.0, 0.0);
    private Point3D upVector = new Point3D(0.0, 1.0, 0.0);
    private final List<Point3D> points;
    private final List<Point3D> bSplinePoints;
    private final List<Ravnina> ravnine;
    private int trenutniSegment = 0;
    private Point3D trenutniVrh;
    private Point3D trenutnaTangenta;
    private double trenutniKut;
    private GLCanvas glCanvas;
    private final int scale = 50;
    private double t = 0;
    private final Point3D os = new Point3D(0,0,0);
    private final Point3D s = new Point3D(0,0, 1);

    public Zad5() {
        points = new ArrayList<>();
        ravnine = new ArrayList<>();
        bSplinePoints = new ArrayList<>();
        File myObj = new File("aircraft747.obj");
        File bSplineFile = new File("bSpline.txt");
        Scanner myReader = null;
        try {
            myReader = new Scanner(myObj);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            if (data.startsWith("v")) {
                String[] koordinate = data.split(" ");
                Point3D point3D = new Point3D(Double.parseDouble(koordinate[1]), Double.parseDouble(koordinate[2]), Double.parseDouble(koordinate[3]));
                points.add(point3D);
            } else if (data.startsWith("f")) {
                String[] ravnina_tocke = data.split(" ");
                Ravnina ravnina = new Ravnina(Integer.parseInt(ravnina_tocke[1]) - 1, Integer.parseInt(ravnina_tocke[2]) - 1, Integer.parseInt(ravnina_tocke[3]) - 1);
                ravnine.add(ravnina);
            }
        }
        myReader.close();


        try {
            myReader = new Scanner(bSplineFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (myReader.hasNextLine()) {
            String data = myReader.nextLine();
            if (data.startsWith("v")) {
                String[] koordinate = data.split(" ");
                Point3D point3D = new Point3D(Double.parseDouble(koordinate[1]), Double.parseDouble(koordinate[2]), Double.parseDouble(koordinate[3]));
                bSplinePoints.add(point3D);
            }
        }
        myReader.close();

        initGLCanvas();
        initListeners();
        initGUI();
    }

    private void racunaj() {
        Point3D p1 = bSplinePoints.get(trenutniSegment);
        Point3D p2 = bSplinePoints.get(trenutniSegment + 1);
        Point3D p3 = bSplinePoints.get(trenutniSegment + 2);
        Point3D p4 = bSplinePoints.get(trenutniSegment + 3);
        double f1 = (-Math.pow(t, 3) + 3 * Math.pow(t, 2) - 3 * t + 1) / 6;
        double f2 = (3 * Math.pow(t, 3) - 6 * Math.pow(t, 2) + 4) / 6;
        double f3 = (-3 * Math.pow(t, 3) + 3 * Math.pow(t, 2) + 3 * t + 1) / 6;
        double f4 = Math.pow(t, 3) / 6;

        trenutniVrh = new Point3D(f1 * p1.getX() + f2 * p2.getX() + f3 * p3.getX() + f4 * p4.getX(),
                f1 * p1.getY() + f2 * p2.getY() + f3 * p3.getY() + f4 * p4.getY(),
                f1 * p1.getZ() + f2 * p2.getZ() + f3 * p3.getZ() + f4 * p4.getZ());

        double t1 = 0.5 * (-Math.pow(t, 2) + 2 * t - 1);
        double t2 = 0.5 * (3 * Math.pow(t, 2) - 4 * t);
        double t3 = 0.5 * (-3* Math.pow(t, 2) + 2 * t + 1);
        double t4 = 0.5 * Math.pow(t, 2);
        trenutnaTangenta = new Point3D(t1 * p1.getX() + t2 * p2.getX() + t3 * p3.getX() + t4*p4.getX(),
                t1 * p1.getY() + t2 * p2.getY() + t3 * p3.getY() + t4 * p4.getY(),
                t1 * p1.getZ() + t2 * p2.getZ() + t3 * p3.getZ() + t4 * p4.getZ());

        Point3D e = trenutnaTangenta;
        os.setX(s.getY() * e.getZ() - e.getY() * s.getZ());
        os.setY(s.getZ() * e.getX() - e.getZ() * s.getX());
        os.setZ(s.getX() * e.getY() - e.getX() * s.getY());

        double absS = Math.sqrt(Math.pow(s.getX(), 2) + Math.pow(s.getY(), 2) + Math.pow(s.getZ(), 2));
        double absE = Math.sqrt(Math.pow(e.getX(), 2) + Math.pow(e.getY(), 2) + Math.pow(e.getZ(), 2));
        double se = s.getX() * e.getX() + s.getY() * e.getY() + s.getZ() * e.getZ();
        double kut = Math.acos(se / (absS*absE));
        trenutniKut = kut / (2*Math.PI) * 360;
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(Zad5::new);
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
        setSize(width, height);
        setVisible(true);
        glCanvas.requestFocusInWindow();
    }

    private void initGLCanvas() {
        GLProfile glProfile = GLProfile.getDefault();
        GLCapabilities glCapabilities = new GLCapabilities(glProfile);
        glCanvas = new GLCanvas(glCapabilities);
    }

    private void initListeners() {
        glCanvas.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                e.consume();
                glCanvas.display();
            }
        });

        glCanvas.addGLEventListener(new GLEventListener() {


            @Override
            public void init(GLAutoDrawable glAutoDrawable) {

            }

            @Override
            public void dispose(GLAutoDrawable glAutoDrawable) {

            }

            @Override
            public void display(GLAutoDrawable glAutoDrawable) {
                GL2 gl2 = glAutoDrawable.getGL().getGL2();
                GLU glu = new GLU();
                gl2.glClear(GL.GL_COLOR_BUFFER_BIT);
                gl2.glLoadIdentity();

                gl2.glPolygonMode(gl2.GL_FRONT_AND_BACK, gl2.GL_LINE);
                gl2.glMatrixMode(gl2.GL_PROJECTION);
                gl2.glLoadIdentity();
                glu.gluPerspective(50, (double) width / height, 0.5, 50);

                glu.gluLookAt(cameraPositon.getX(), cameraPositon.getY(), cameraPositon.getZ(),
                        lookAt.getX(), lookAt.getY(), lookAt.getZ(),
                        upVector.getX(), upVector.getY(), upVector.getZ());

                gl2.glBegin(gl2.GL_LINE_STRIP);
                gl2.glColor3d(0, 0, 1);
                for (int i = 0; i < bSplinePoints.size() - 3; i++) {
                    Point3D p1 = bSplinePoints.get(i);
                    Point3D p2 = bSplinePoints.get(i + 1);
                    Point3D p3 = bSplinePoints.get(i + 2);
                    Point3D p4 = bSplinePoints.get(i + 3);
                    for (double t = 0; t < 1; t += 0.05) {
                        double f1 = (-Math.pow(t, 3) + 3 * Math.pow(t, 2) - 3 * t + 1) / 6;
                        double f2 = (3 * Math.pow(t, 3) - 6 * Math.pow(t, 2) + 4) / 6;
                        double f3 = (-3 * Math.pow(t, 3) + 3 * Math.pow(t, 2) + 3 * t + 1) / 6;
                        double f4 = Math.pow(t, 3) / 6;

                        Point3D vrh = new Point3D(f1 * p1.getX() + f2 * p2.getX() + f3 * p3.getX() + f4 * p4.getX(),
                                f1 * p1.getY() + f2 * p2.getY() + f3 * p3.getY() + f4 * p4.getY(),
                                f1 * p1.getZ() + f2 * p2.getZ() + f3 * p3.getZ() + f4 * p4.getZ());

                        double t1 = 0.5 * (-Math.pow(t, 2) + 2 * t - 1);
                        double t2 = 0.5 * (3 * Math.pow(t, 2) - 4 * t);
                        double t3 = 0.5 * (-3 * Math.pow(t, 2) + 2 * t + 1);
                        double t4 = 0.5 * Math.pow(t, 2);

                        Point3D tangenta = new Point3D(t1 * p1.getX() + t2 * p2.getX() + t3 * p3.getX() + t4 * p4.getX(),
                                t1 * p1.getY() + t2 * p2.getY() + t3 * p3.getY() + t4 * p4.getY(),
                                t1 * p1.getZ() + t2 * p2.getZ() + t3 * p3.getZ() + t4 * p4.getZ());

                        gl2.glVertex3d(vrh.getX() / scale, vrh.getY() / scale, vrh.getZ() / scale);
                        gl2.glVertex3d((vrh.getX() + tangenta.getX()) / scale, (vrh.getY() + tangenta.getY()) / scale, (vrh.getZ() + tangenta.getZ()) / scale);
                    }
                }
                gl2.glEnd();

                racunaj();
                gl2.glColor3d(1, 0, 0);
                gl2.glBegin(gl2.GL_LINE_STRIP);
                gl2.glVertex3d(trenutniVrh.getX() / scale, trenutniVrh.getY() / scale, trenutniVrh.getZ() / scale);
                gl2.glVertex3d((trenutniVrh.getX() + trenutnaTangenta.getX()) / 1, (trenutniVrh.getY() + trenutnaTangenta.getY()) / 1, (trenutniVrh.getZ() + trenutnaTangenta.getZ()) / 1);
                gl2.glEnd();
                gl2.glTranslated(trenutniVrh.getX() / scale, trenutniVrh.getY() / scale, trenutniVrh.getZ() / scale);
                gl2.glScaled(1 / 4.0, 1 / 4.0, 1 / 4.0);
                gl2.glRotated(trenutniKut, os.getX(), os.getY(), os.getZ());

                gl2.glBegin(GL.GL_TRIANGLES);
                for (Ravnina ravnina : ravnine) {
                    double x1 = points.get(ravnina.getA()).getX();
                    double y1 = points.get(ravnina.getA()).getY();
                    double z1 = points.get(ravnina.getA()).getZ();
                    double x2 = points.get(ravnina.getB()).getX();
                    double y2 = points.get(ravnina.getB()).getY();
                    double z2 = points.get(ravnina.getB()).getZ();
                    double x3 = points.get(ravnina.getC()).getX();
                    double y3 = points.get(ravnina.getC()).getY();
                    double z3 = points.get(ravnina.getC()).getZ();


                    gl2.glVertex3d(x1, y1, z1);
                    gl2.glVertex3d(x2, y2, z2);
                    gl2.glVertex3d(x3, y3, z3);
                }
                gl2.glEnd();

                t += 0.05;
                if (t >= 1) {
                    t = 0;
                    trenutniSegment += 1;
                    if (trenutniSegment >= bSplinePoints.size() - 3) {
                        trenutniSegment = 0;
                    }
                }
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


}
