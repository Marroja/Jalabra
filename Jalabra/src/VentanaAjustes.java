//import sun.font.FontFamily;

import javax.sound.sampled.Line;
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;

public class VentanaAjustes extends JFrame {

    private JLabel LPrevistaClaro, LPrevistaObscuro;
    private JTextPane TBPrevistaClaro, TBPrevistaObscuro;
    private JLabel LTextoClaro, LTextoObscuro;
    private JButton[][] BColorTema;
    private JSlider STextoGrande, STextoChico;
    private JComboBox CBFuentes, CBIdioma;


    public VentanaAjustes(){
        this.setSize(new Dimension(400,600));
        this.setResizable(false);
        this.setVisible(true);
        this.setLayout(null);

        Controles();
        Formato();
    }

    private void Controles(){

        this.setTitle("Ajustes");

        JLabel LFuentes = new JLabel("Fuentes:");
        LFuentes.setBounds(15, 190, 70, 30);
        this.add(LFuentes);

        JLabel LIdioma = new JLabel("Idiomas:");
        LIdioma.setBounds(240, 190, 70, 30);
        this.add(LIdioma);

        CBIdioma = new JComboBox(Herr.leerLineaArchivo("lingua.txt",0).split("\t"));
        CBIdioma.setSelectedItem(Herr.leerLineaArchivo("config.txt",1,0));
        CBIdioma.setBounds(300, 190, 70, 30);
        CBIdioma.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Formato();
            }
        });
        this.add(CBIdioma);

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        CBFuentes = new JComboBox(ge.getAvailableFontFamilyNames());
        CBFuentes.setSelectedItem(Herr.leerLineaArchivo("config.txt",6,0));
        CBFuentes.setBounds(70, 190, 150, 30);
        CBFuentes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Formato();
            }
        });
        this.add(CBFuentes);

        JButton BActualizar = new JButton("Aplicar");
        BActualizar.setBounds(270, 520, 100, 30);
        BActualizar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] texto = {Herr.leerLineaArchivo("config.txt",0),
                                CBIdioma.getSelectedItem().toString(),
                                Integer.toHexString(BColorTema[0][0].getBackground().getRGB()).substring(2)+"\t"+
                                Integer.toHexString(BColorTema[1][0].getBackground().getRGB()).substring(2)+"\t"+
                                Integer.toHexString(BColorTema[2][0].getBackground().getRGB()).substring(2)+"\t"+
                                Integer.toHexString(BColorTema[3][0].getBackground().getRGB()).substring(2)+"\t"+
                                Integer.toHexString(BColorTema[4][0].getBackground().getRGB()).substring(2),
                                Integer.toHexString(BColorTema[0][1].getBackground().getRGB()).substring(2)+"\t"+
                                Integer.toHexString(BColorTema[1][1].getBackground().getRGB()).substring(2)+"\t"+
                                Integer.toHexString(BColorTema[2][1].getBackground().getRGB()).substring(2)+"\t"+
                                Integer.toHexString(BColorTema[3][1].getBackground().getRGB()).substring(2)+"\t"+
                                Integer.toHexString(BColorTema[4][1].getBackground().getRGB()).substring(2),
                                Herr.leerLineaArchivo("config.txt",4),
                                Herr.leerLineaArchivo("config.txt",5),
                                CBFuentes.getSelectedItem().toString(),
                                Herr.leerLineaArchivo("config.txt",7),
                                STextoGrande.getValue()+"\t"+STextoChico.getValue(),
                                Herr.leerLineaArchivo("config.txt",9),
                                Herr.leerLineaArchivo("config.txt",10),
                                Herr.leerLineaArchivo("config.txt",11),
                                Herr.leerLineaArchivo("config.txt",12)
                                };
                try {
                    Herr.cambiarArchivo("config.txt", Arrays.asList(texto));
                }catch (IOException f){
                    System.err.println("Error al escribir los cambios en config.txt");
                }
                setVisible(false);
                dispose();
            }
        });

        JLabel LTextoGrande = new JLabel("Texto etiquetas");
        //LTextoGrande.setFont(new Font("",0,20));
        LTextoGrande.setBounds(20, 230, 200, 20);


        STextoGrande = new JSlider();
        STextoGrande.setBounds(45, 250, 300, 50);
        STextoGrande.setMinimum(5);
        STextoGrande.setMaximum(80);
        STextoGrande.setValue(Integer.parseInt(Herr.leerLineaArchivo("config.txt",8, 0)));
        STextoGrande.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Formato();
            }
        });
        STextoGrande.setMajorTickSpacing(10);
        STextoGrande.setMinorTickSpacing(1);
        STextoGrande.setPaintTicks(true);
        STextoGrande.setPaintLabels(true);


        JLabel LTextoChico = new JLabel("Texto ventanas");
        //LTextoChico.setFont(new Font("",0,20));
        LTextoChico.setBounds(20, 300, 200, 20);

        STextoChico = new JSlider();
        STextoChico.setToolTipText("Tamaño texto ventanas");
        STextoChico.setBounds(45, 320, 300, 50);
        STextoChico.setMinimum(5);
        STextoChico.setMaximum(50);
        STextoChico.setValue(Integer.parseInt(Herr.leerLineaArchivo("config.txt",8, 1)));
        STextoChico.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Formato();
            }
        });
        STextoChico.setMajorTickSpacing(10);
        STextoChico.setMinorTickSpacing(1);
        STextoChico.setPaintTicks(true);
        STextoChico.setPaintLabels(true);

        JLabel LTemaObscuro = new JLabel("Tema obscuro");
        LTemaObscuro.setBounds(15, 370, 100, 30);


        JLabel LTemaClaro = new JLabel("Tema claro");
        LTemaClaro.setBounds(220, 370, 100, 30);


        BColorTema = new JButton[5][2];
        for(int i = 0; i<5; i++){
            BColorTema[i][0] = new JButton();
            BColorTema[i][0].setBounds(15 + 32*i, 400, 30, 30);
            BColorTema[i][0].setBackground(new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt",2, i),16)));
            this.add(BColorTema[i][0]);

            BColorTema[i][1] = new JButton();
            BColorTema[i][1].setBounds(220 + 32*i, 400, 30, 30);
            BColorTema[i][1].setBackground(new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt",3, i),16)));
            this.add(BColorTema[i][1]);

            BColorTema[i][0].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton boton = (JButton)e.getSource();
                    Color colorTemp = JColorChooser.showDialog(null, "Elija el color", boton.getBackground());
                    boton.setBackground(colorTemp);
                    Formato();
                }
            });
            BColorTema[i][1].addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton boton = (JButton)e.getSource();
                    Color colorTemp = JColorChooser.showDialog(null, "Elija el color", boton.getBackground());
                    boton.setBackground(colorTemp);
                    Formato();
                }
            });
        }

        JButton[][] BColorEstudio = new JButton[5][3];

        LPrevistaObscuro = new JLabel();
        LPrevistaObscuro.setOpaque(true);
        LPrevistaObscuro.setBounds(20, 30, 160, 150);
        LPrevistaClaro = new JLabel();
        LPrevistaClaro.setOpaque(true);
        LPrevistaClaro.setBounds(200, 30, 160, 150);

        LTextoObscuro = new JLabel("Tema obscuro");
        LTextoObscuro.setBounds(30, 25, 150, 60);
        LTextoClaro = new JLabel("Tema claro");
        LTextoClaro.setBounds(30 + 180, 25, 150, 60);

        TBPrevistaObscuro = new JTextPane();
        TBPrevistaObscuro.setText("Texto de ejemplo para el tema");
        TBPrevistaObscuro.setBounds(45, 80, 110, 80);
        TBPrevistaClaro = new JTextPane();
        TBPrevistaClaro.setText("Texto de ejemplo para el tema");
        TBPrevistaClaro.setBounds(45 + 180, 80, 110, 80);

        LTextoGrande.setFont(new Font(CBFuentes.getSelectedItem().toString(),0,20));
        LTextoChico.setFont(new Font(CBFuentes.getSelectedItem().toString(),0,20));

        if(Files.exists(Paths.get(Paths.get("").toAbsolutePath().toString()+"/lingua.txt"))) {
            //pregunta = Herr.leerLineaArchivo("lingua.txt", 10, numColumnaIdioma);
            int idioma = CBIdioma.getSelectedIndex();
            this.setTitle(Herr.leerLineaArchivo("lingua.txt",30,idioma));

            LTemaObscuro.setText(Herr.leerLineaArchivo("lingua.txt",31,idioma));
            LTemaClaro.setText(Herr.leerLineaArchivo("lingua.txt",32,idioma));
            TBPrevistaClaro.setText(Herr.leerLineaArchivo("lingua.txt",32,idioma));
            TBPrevistaObscuro.setText(Herr.leerLineaArchivo("lingua.txt",31,idioma));
            LTextoClaro.setText(Herr.leerLineaArchivo("lingua.txt",32,idioma));
            LTextoObscuro.setText(Herr.leerLineaArchivo("lingua.txt",31,idioma));
            LFuentes.setText(Herr.leerLineaArchivo("lingua.txt",33,idioma));
            LIdioma.setText(Herr.leerLineaArchivo("lingua.txt",34,idioma));
            LTextoGrande.setText(Herr.leerLineaArchivo("lingua.txt",35,idioma));
            LTextoChico.setText(Herr.leerLineaArchivo("lingua.txt",36,idioma));
            BActualizar.setText(Herr.leerLineaArchivo("lingua.txt",37,idioma));
        }


        this.add(BActualizar);
        this.add(LTextoGrande);
        this.add(STextoGrande);
        this.add(LTextoChico);
        this.add(STextoChico);
        this.add(LTemaObscuro);
        this.add(LTemaClaro);
        this.add(LTextoClaro);
        this.add(LTextoObscuro);
        this.add(TBPrevistaClaro);
        this.add(TBPrevistaObscuro);
        this.add(LPrevistaClaro);
        this.add(LPrevistaObscuro);
    }

    private void Formato(){
        //(fondo-letras-marcos-fondoTexto-textoListas)



        LPrevistaClaro.setVisible(false);
        LPrevistaObscuro.setVisible(false);
        LTextoClaro.setVisible(false);
        LTextoObscuro.setVisible(false);
        TBPrevistaClaro.setVisible(false);
        TBPrevistaObscuro.setVisible(false);

        LPrevistaObscuro.setBackground(BColorTema[0][0].getBackground());
        LPrevistaObscuro.setBorder(new LineBorder(BColorTema[2][0].getBackground(),3));
        LPrevistaClaro.setBackground(BColorTema[0][1].getBackground());
        LPrevistaClaro.setBorder(new LineBorder(BColorTema[2][1].getBackground(),3));
        LPrevistaClaro.validate();
        LPrevistaObscuro.validate();
        LPrevistaClaro.setVisible(true);
        LPrevistaObscuro.setVisible(true);

        LTextoClaro.setFont(new Font(CBFuentes.getSelectedItem().toString(),0,STextoGrande.getValue()));
        LTextoObscuro.setFont(new Font(CBFuentes.getSelectedItem().toString(),0,STextoGrande.getValue()));
        LTextoClaro.setForeground(BColorTema[1][1].getBackground());
        LTextoObscuro.setForeground(BColorTema[1][0].getBackground());
        LTextoClaro.validate();
        LTextoObscuro.validate();
        LTextoClaro.setVisible(true);
        LTextoObscuro.setVisible(true);

        TBPrevistaObscuro.setBackground(BColorTema[3][0].getBackground());
        TBPrevistaObscuro.setBorder(new LineBorder(BColorTema[2][0].getBackground(),2));
        TBPrevistaObscuro.setForeground(BColorTema[4][0].getBackground());
        TBPrevistaObscuro.setFont(new Font(CBFuentes.getSelectedItem().toString(),0,STextoChico.getValue()));

        TBPrevistaClaro.setBackground(BColorTema[3][1].getBackground());
        TBPrevistaClaro.setBorder(new LineBorder(BColorTema[2][1].getBackground(),2));
        TBPrevistaClaro.setForeground(BColorTema[4][1].getBackground());
        TBPrevistaClaro.setFont(new Font(CBFuentes.getSelectedItem().toString(),0,STextoChico.getValue()));

        TBPrevistaClaro.validate();
        TBPrevistaObscuro.validate();
        TBPrevistaClaro.setVisible(true);
        TBPrevistaObscuro.setVisible(true);

        update(this.getGraphics());
    }
}
