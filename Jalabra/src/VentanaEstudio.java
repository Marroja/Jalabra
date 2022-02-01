import javax.swing.*;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import javax.sound.sampled.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.jsoup.Jsoup;
import uk.co.caprica.vlcj.player.component.EmbeddedMediaListPlayerComponent;


public class VentanaEstudio extends JFrame {

    private int modoOscuro;
    private final String archivoVoc;
    private final String archivoHist;
    private final String directorioRecs;
    private int ancho, alto;
    private JButton BPista, BModoEstudio, BModoObscuro, BSiguiente;
    private String fuente;
    private Color colorCorrecto, colorPista, colorIncorrecto;
    private JLabel LOración;
    private JTextField TFRespuesta;
    private int numTotalModos, numModo;
    private String respuestaEsperada;
    private final int numColumnaIdioma;
    private Clip clip;
    private JSlider SVolumen;
    private JLabel LVolumen;
    private EmbeddedMediaListPlayerComponent reproductorMedios;

    /*En esta ventana uno pasa en principio la mayor parte del tiempo, cuando uno importa su biblioteca de
    palabras, en esta ventana es donde uno las estudia y repite una y otra vez. Se llama a Herr para revisar la
    respuesta adecuada para cada tipo de problema y demás, los colores se sacan del archivo config.txt y al cerrar
    esta ventana uno regresa a la ventana principal. El plan es que por medio de esta misma forma uno pueda
    obtener oraciones ejemplo de Tatoeba (por descarga, ya que no hay API) e imágenes de Yandex, Google o Bing
    también por medio de una bandera que se pueda acceder a imágenes guardadas en una sub-carpeta en
    /recs/baraja/palabra.jpg o algo por el estilo*/

    /*El formato de los archivos de la baraja son palabra\t cosa1 \t cosa2 \t... cosaN
    * Por modo de estudio determina cuáles dos cosas se toman, como pregunta (se muestra en el JLabel) y como
    * respuesta (se revisa desde el JTextField). El archivo tiene en la primera línea los indicadores de qué columna
    * se preguntará y qué pregunta será su correspondiente respuesta. "%Kanji" es una etiqueta válida para pregunta,
    * su correspondiente respuesta deberá estar etiquetada con "$Kanji", el análisis de las etiquetas se hará por
    * medio de Herr. Esa etiqueta se tomará tal cual para cambiar el texto del botón BModo*/

    public VentanaEstudio(String directorioJar, String nombreBaraja){
        this.archivoVoc = directorioJar+"voc/"+nombreBaraja;
        this.archivoHist = directorioJar+"hist/"+nombreBaraja;
        this.directorioRecs = directorioJar+"recs/"+nombreBaraja;

        if(Files.exists(Paths.get("lingua.txt"))){
            String idioma = Herr.leerLineaArchivo("config.txt",1);
            String listaIdiomas = Herr.leerLineaArchivo("lingua.txt",0);
            numColumnaIdioma = Herr.numColumna(idioma, listaIdiomas);
        }else{
            numColumnaIdioma = -1;
        }

        Inicializar();
        Controles();
        FormatoConfig();
        Formato();
        ActualizarPalabra();
        this.setTitle(archivoVoc);
        setLocationRelativeTo(null);
        setLayout(null);
        setVisible(true);
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);

    }


    private void Controles() {
        //Inicializando todos los botones parejo... cambiarán el texto de acuerdo a FormatoConfig excepto BModo
        //TFRespuesta también cambiará de color por medio de este método

        TFRespuesta = new JTextField();
        TFRespuesta.setHorizontalAlignment(JLabel.CENTER);
        this.add(TFRespuesta);
        BModoEstudio = new JButton("Modo estudio "+numModo);
        this.add(BModoEstudio);
        BPista = new JButton("Respuesta");
        this.add(BPista);
        BSiguiente = new JButton("Siguiente");
        this.add(BSiguiente);
        BModoObscuro = new JButton("Modo obscuro");
        this.add(BModoObscuro);
        LOración = new JLabel();
        LOración.setHorizontalAlignment(JLabel.CENTER);
        LOración.setVerticalAlignment(JLabel.CENTER);
        this.add(LOración);

        this.addWindowListener(new WindowListener() {
            @Override
            public void windowOpened(WindowEvent e) {

            }

            @Override
            public void windowClosing(WindowEvent e) {
                if(clip!=null){
                    clip.close();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                if(clip!=null){
                    clip.close();
                }

            }

            @Override
            public void windowIconified(WindowEvent e) {

            }

            @Override
            public void windowDeiconified(WindowEvent e) {

            }

            @Override
            public void windowActivated(WindowEvent e) {

            }

            @Override
            public void windowDeactivated(WindowEvent e) {

            }
        });

        LVolumen = new JLabel();
        this.add(LVolumen);

        SVolumen = new JSlider();
        SVolumen.setMaximum(5);
        SVolumen.setMinimum(-50);
        SVolumen.setValue((Integer.parseInt(Herr.leerLineaArchivo("config.txt",10))));
        this.add(SVolumen);

        SVolumen.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                Herr.cambiarLineaArchivo("config.txt",10,""+SVolumen.getValue());

                clip.setMicrosecondPosition(clip.getMicrosecondPosition());
                clip.stop();

                FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(Integer.parseInt(Herr.leerLineaArchivo("config.txt",10)));

                clip.start();
            }
        });

        BSiguiente.addActionListener(e -> {
            if(RevisarRespuesta()){
                TFRespuesta.setBackground(Color.WHITE);
                TFRespuesta.setText("");
                Herr.actualizarHistorial(archivoHist, Herr.indicePalabraMenosEstudiada(archivoHist));
                ActualizarPalabra();
            }
            else{
                TFRespuesta.setBackground(colorIncorrecto);
            }
        });

        BModoObscuro.addActionListener(e -> {
            if(modoOscuro == 1) {
                Herr.cambiarLineaArchivo("config.txt", 0, "0");
                modoOscuro = 0;
            }
            else{
                Herr.cambiarLineaArchivo("config.txt", 0, "1");
                modoOscuro = 1;
            }
            FormatoConfig();
        });

        BPista.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }

            @Override
            public void mousePressed(MouseEvent e) {
                TFRespuesta.setText(respuestaEsperada);
                TFRespuesta.setBackground(colorPista);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                TFRespuesta.setText("");
                TFRespuesta.setBackground(Color.WHITE);
            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                ancho = getWidth();
                alto = getHeight();
                Formato();
                try {
                    Herr.cambiarArchivoUnaLinea("config.txt", 5, ancho + "\t" + alto);
                }
                catch(IOException e){
                    System.err.println("No se pudo guardar el tamaño preferido");
                }
            }
        });

        TFRespuesta.addKeyListener(new KeyListener() {

            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ALT){
                    TFRespuesta.setText(respuestaEsperada);
                    TFRespuesta.setBackground(colorPista);
                    e.consume();
                }
                if(e.getKeyCode() == KeyEvent.VK_CONTROL){
                    BModoEstudio.doClick();
                }
                TFRespuesta.requestFocus();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ALT) {
                    TFRespuesta.setText("");
                    TFRespuesta.setBackground(Color.WHITE);
                }
                TFRespuesta.requestFocus();
            }

            @Override
            public void keyTyped(KeyEvent e) {
                TFRespuesta.requestFocus();
            }


        });

        BModoEstudio.addActionListener(e -> {
            if(numModo < numTotalModos-1){
                numModo++;
            }
            else{
                numModo = 0;
            }
            FormatoConfig();
            ActualizarPalabra();
            TFRespuesta.setText("");
        });

        TFRespuesta.addActionListener(e -> {
            if(RevisarRespuesta() && TFRespuesta.getBackground() == colorCorrecto){
                TFRespuesta.setBackground(Color.WHITE);
                TFRespuesta.setText("");
                Herr.actualizarHistorial(archivoHist, Herr.indicePalabraMenosEstudiada(archivoHist));
                ActualizarPalabra();
            }else if(RevisarRespuesta()){
                TFRespuesta.setBackground(colorCorrecto);
            }
            else{
                TFRespuesta.setBackground(colorIncorrecto);
            }

        });
    }

    private void FormatoConfig(){

        modoOscuro = Integer.parseInt(Herr.leerLineaArchivo("config.txt",0,0));
        //Si oscuro está prendido (1) entonces al restar pasa a la línea de texto 2, que es la que corresponde con config modoObscuro
        Color colorFondo = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 3 - modoOscuro, 0), 16));
        Color colorTexto = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 3 - modoOscuro, 1), 16));
        Color colorMarcos = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 9, numModo), 16));

        TFRespuesta.setBorder(new LineBorder(colorMarcos, 2));
        BModoEstudio.setBorder(new LineBorder(colorMarcos, 2));
        BModoEstudio.setForeground(colorTexto);
        BModoEstudio.setBackground(colorFondo);
        BPista.setBackground(colorFondo);
        BPista.setBorder(new LineBorder(colorMarcos, 2));
        BPista.setForeground(colorTexto);
        BSiguiente.setBackground(colorFondo);
        BSiguiente.setBorder(new LineBorder(colorMarcos, 2));
        BSiguiente.setForeground(colorTexto);
        BModoObscuro.setBackground(colorFondo);
        BModoObscuro.setBorder(new LineBorder(colorMarcos, 2));
        BModoObscuro.setForeground(colorTexto);
        LOración.setBorder(new LineBorder(colorMarcos, 25));
        LOración.setOpaque(true);
        LOración.setBackground(colorFondo);
        LOración.setForeground(colorTexto);
        SVolumen.setForeground(colorTexto);
        SVolumen.setBackground(colorFondo);
        LVolumen.setForeground(colorTexto);

        if(numColumnaIdioma != -1){
            BPista.setText(Herr.leerLineaArchivo("lingua.txt",20, numColumnaIdioma));
            BSiguiente.setText(Herr.leerLineaArchivo("lingua.txt",21, numColumnaIdioma));
            BModoObscuro.setText(Herr.leerLineaArchivo("lingua.txt",22, numColumnaIdioma));
            SVolumen.setToolTipText(Herr.leerLineaArchivo("lingua.txt",24, numColumnaIdioma));
            LVolumen.setText(Herr.leerLineaArchivo("lingua.txt",24, numColumnaIdioma));
        }

        this.getContentPane().setBackground(colorFondo);
    }

    private void Formato(){
        TFRespuesta.setBounds(ancho/3, alto* 2 / 3, ancho/ 3, alto/8);
        TFRespuesta.setFont(new Font(fuente, Font.PLAIN, ancho/40));
        BModoEstudio.setBounds(ancho/3,alto*19/24+5, ancho/9, alto/12);
        BModoEstudio.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        BPista.setBounds(ancho/3+ancho/9,alto*19/24+5,ancho/9, alto/12);
        BPista.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        BSiguiente.setBounds(ancho/3+ancho*2/9,alto*19/24+5,ancho/9, alto/12);
        BSiguiente.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        BModoObscuro.setBounds(ancho/3+ancho/9,alto*21/24+5, ancho/9, alto/24);
        BModoObscuro.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        LOración.setBounds(ancho/12, 40,10*ancho/12,alto/2);
        LOración.setFont(new Font(fuente, Font.PLAIN, ancho/20));
        LVolumen.setBounds(9*ancho/12, 11*alto/16, ancho/12, alto/24);
        SVolumen.setBounds(9*ancho/12, 6*alto/8, ancho/12, alto/24);
        LVolumen.setFont(new Font(fuente, Font.PLAIN, ancho/90));
    }

    private void ActualizarPalabra(){
        String primeraLinea = Herr.leerLineaArchivo(archivoVoc, 0);

        if(clip != null){
            if(clip.isActive()){
                clip.close();
            }
        }



        /*Se lee la primera línea y se parte a la mitad donde se encuentra %Modo
        * Luego se cuentan la cantidad de tabs que quedaron en la mitad antes de %Modo
        * Ese número es igual al número de columnas*/
        String bandera = "%"+numModo;
        int numColumnaPregunta = Herr.numColumna(bandera, primeraLinea);
        bandera = "!"+numModo;
        int numColumnaRespuesta = Herr.numColumna(bandera, primeraLinea);
        String[] preguntaRespuesta = Herr.siguientePreguntaRespuesta(archivoVoc,archivoHist, numColumnaPregunta, numColumnaRespuesta);

        String textoA = "-->";
        if(Files.exists(Paths.get("lingua.txt"))){
            textoA = Herr.leerLineaArchivo("lingua.txt",23, numColumnaIdioma);
        }

        if(numColumnaIdioma > -1){
            String textoTemp = Herr.leerLineaArchivo(archivoVoc,1,numColumnaPregunta) + " " +
                    textoA + " " +
                    Herr.leerLineaArchivo(archivoVoc, 1, numColumnaRespuesta);
            BModoEstudio.setText(textoTemp);
        }

        System.out.println("Pregunta en columna:"+numColumnaPregunta);
        System.out.println("Respuesta en columna:"+numColumnaRespuesta);

        SVolumen.setVisible(false);
        LVolumen.setVisible(false);
        //Si la pregunta lleva a un archivo, lleva a /recs/nombreVoc/archivo.png, jpg, gif...
        //No he logrado echar a andar el audio o el video pero chance es cosa de IntelliJ o de mi compu
        if(preguntaRespuesta[0].contains(".jpg")||preguntaRespuesta[0].contains(".png")||preguntaRespuesta[0].contains(".ico")
                ||preguntaRespuesta[0].contains(".gif")||preguntaRespuesta[0].contains(".tiff")||preguntaRespuesta[0].contains(".bmp")){
            System.out.println("Extrayendo imagen:"+preguntaRespuesta[0]);
            LOración.setText("");
            LOración.setIcon(new ImageIcon(directorioRecs+"/"+preguntaRespuesta[0]));
        }
        else if(preguntaRespuesta[0].contains(".wav")){
            LOración.setText("");
            LOración.setIcon(new ImageIcon(""));
            try{
                File file = new File(directorioRecs+"/"+preguntaRespuesta[0]);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(file);
                clip = AudioSystem.getClip();
                clip.open(audioStream);


                FloatControl volume = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volume.setValue(Integer.parseInt(Herr.leerLineaArchivo("config.txt",10)));

                clip.start();

                clip.loop(Integer.parseInt(Herr.leerLineaArchivo("config.txt",11)));
                SVolumen.setVisible(true);
                LVolumen.setVisible(true);

            } catch (LineUnavailableException | IOException |UnsupportedAudioFileException e) {
                LOración.setText("Error-400");
            }
        }
        else{
            LOración.setIcon(new ImageIcon(""));
            LOración.setText(preguntaRespuesta[0]);
        }


        respuestaEsperada = preguntaRespuesta[1];
        System.out.println("Respuesta esperada:"+respuestaEsperada);

    }

    private Boolean RevisarRespuesta(){
        if(TFRespuesta.getText().trim().equals(respuestaEsperada) || TFRespuesta.getText().trim().equals("")){
            System.out.println("Respuesta correcta");
            return true;
        }
        else{
            System.out.println("Respuesta incorrecta");
            return false;
        }
    }

    private void Inicializar(){

        ancho = Integer.parseInt(Herr.leerLineaArchivo("config.txt",5,0));
        alto = Integer.parseInt(Herr.leerLineaArchivo("config.txt",5, 1));
        fuente = Herr.leerLineaArchivo("config.txt",6,0);

        modoOscuro = Integer.parseInt(Herr.leerLineaArchivo("config.txt", 0, 0));

        numModo = 0;
        String textoPrimeraLinea = Herr.leerLineaArchivo(archivoVoc, 0);
        //String textoPrimeraLinea = "?0 !1\t!0\t?1";
        System.out.println("Primera línea: "+textoPrimeraLinea);
        numTotalModos = (int)textoPrimeraLinea.chars().filter(ch -> ch == '%').count();
        System.out.println("Hay "+numTotalModos+" modos de estudio");

        colorCorrecto = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 7, 0),16));
        colorPista = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 7, 1),16));
        colorIncorrecto = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 7, 2),16));

        setMinimumSize(new Dimension(800,480));
        setTitle(archivoVoc);
        setSize(new Dimension(ancho, alto));
        setLocationRelativeTo(null);
        setVisible(true);
    }


}
