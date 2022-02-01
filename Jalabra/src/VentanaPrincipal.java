
import javax.swing.*;
import javax.swing.border.LineBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import static java.nio.file.StandardCopyOption.*;
import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

public class VentanaPrincipal extends JFrame {

    private int ancho;
    private int alto;
    private String fuente;
    private Color colorTexto;
    private Color colorFondo;
    private String dirVoc, dirHist, dirRecs;
    private JTextPane TBListaPalabras, TBListaEstudiadas;
    private JTextField TFBuscar;
    private JLabel LBarajas, LInfoBaraja, LVecesEstudiadas;
    private JScrollPane SPBarajas, SPPalabras, SPEstudiadas;
    private JButton BBorrar, BEstudiar, BReiniciar, BModoObscuro, BEditar, BAjustes;
    private JList LListaBarajas;
    private int numColumnaIdioma;

    //Esta ventana será la ventana donde se consultan carpetas de vocabulario en una columna a la izquierda
    //Y se verá el antes llamado documento 00.txt (ahora nombreArchivo+"00.txt") a la derecha
    //En la columna de la izquierda se elige y borra carpeta
    //En la columna de la derecha se visualizan y se borran palabras (directo del archivo importado)
    //Todos los controles se tienen que acomodar al tamaño de la ventana sin exagerar.
    //Probablemente valga la pena tener un archivo config.txt o algo así del cual extraer modoObscuro y tamañoIdeal
    //La ventana de estudio emerge de aquí al seleccionar una carpeta y picar "Estudiar" o su equivalente
    //No tiene caso agregar palabras por medio de esta aplicación, mejor hacerlo por Excel o el bloc de notas.

    public VentanaPrincipal(){
        Inicializar();
        Controles();
        FormatoConfig();
        Formato();
        setLocationRelativeTo(null);
        setVisible(true);
        setLayout(null);
        UIManager.put("Button.defaultButtonFollowsFocus", Boolean.TRUE);
    }

    //Método para agregar todos los controles a la forma.
    //En este método también se trabajan todas las interacciones que tienen los controles
    //Por ejemplo, clics, todos se manejan aquí
    //Este método solo se ejecuta una vez al abrir la ventana, para los cambios de formato se usa el método formato
    private void Controles() {

        //Hasta arriba medio a la derecha va la barra de búsqueda
        TFBuscar = new JTextField();
        TFBuscar.setEditable(true);
        this.add(TFBuscar);

        TFBuscar.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER){
                    TBListaPalabras.getText();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });

        //Por necesidad del código, hay que instanciar primero la lista de palabras
        TBListaPalabras = new JTextPane();
        TBListaPalabras.setEditable(false);
        this.add(TBListaPalabras);

        //Agregando el deslizador
        SPPalabras = new JScrollPane();
        this.add(SPPalabras, BorderLayout.CENTER);

        //Por necesidad del código, hay que declarar primero la lista de palabras
        TBListaEstudiadas = new JTextPane();
        TBListaEstudiadas.setEditable(false);
        this.add(TBListaEstudiadas);

        //Agregando el deslizador
        SPEstudiadas = new JScrollPane();
        this.add(SPEstudiadas);

        //Label hasta arriba izquierda "Lista de barajas"
        LBarajas = new JLabel("Lista de barajas:");
        this.add(LBarajas);
        LInfoBaraja = new JLabel("Baraja:");
        this.add(LInfoBaraja);
        LVecesEstudiadas = new JLabel("Veces estudiadas:");
        this.add(LVecesEstudiadas);

        //Inicializando y agregando todas las cosas de izquierda a derecha
        LListaBarajas = new JList();
        try{
            if(new File(dirVoc).list().length > 0){
                LListaBarajas = new JList(new File(dirVoc).list());
            }
        }catch (NullPointerException n){
            System.err.println("No hay archivos a importar");
        }

        this.add(LListaBarajas);
        LListaBarajas.addListSelectionListener(new ListSelectionListener() {
            //Con el movimiento de selección en la columna de la izquierda uno elige lista de palabras
            //Se lee el archivo del que viene el nombre escrito en la columna izquierda
            //Y se lee el archivo igual /voc/baraja y /hist/baraja para encontrar también las veces estudiadas
            //Si falta el archivo /hist/baraja se crea en el momento
            @Override
            public void valueChanged(ListSelectionEvent e) {
                try{
                    if(LListaBarajas.getSelectedValue() != null){
                        String textoTemp;
                        Path directVoc = Paths.get(dirVoc+"/"+LListaBarajas.getSelectedValue().toString());
                        //System.out.println(directVoc.toAbsolutePath());
                        textoTemp = String.join("\n",Files.readAllLines(directVoc,StandardCharsets.UTF_16LE)).replace("\uFEFF","");
                        TBListaPalabras.setText(textoTemp);
                        TBListaPalabras.setCaretPosition(0);
                        SPPalabras.getVerticalScrollBar().setValue(0);

                        Path directHist = Paths.get(dirHist+"/"+LListaBarajas.getSelectedValue().toString());
                        if(Files.exists(directHist)){

                        }
                        else{
                            Herr.fabricarArchivo00(directVoc.toString(),directHist.toString());
                        }
                        textoTemp = String.join("\n",Files.readAllLines(directHist,StandardCharsets.UTF_16LE)).replace("\uFEFF","");
                        TBListaEstudiadas.setText(textoTemp);
                        TBListaEstudiadas.setCaretPosition(0);
                        SPEstudiadas.getVerticalScrollBar().setValue(0);
                        Formato();
                    }

                }catch(IOException f){
                    System.out.println("Error al leer el archivo seleccionado");
                    f.printStackTrace();
                }
            }
        });

        LListaBarajas.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
                if(e.getKeyChar() == KeyEvent.VK_ENTER){
                    BEstudiar.doClick();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {

            }

            @Override
            public void keyReleased(KeyEvent e) {

            }
        });
        //LListaBarajas.setSelectedIndex(0);

        LListaBarajas.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println("Actualización por clic");
                int indiceTemporal = LListaBarajas.getSelectedIndex();
                LListaBarajas.clearSelection();
                LListaBarajas.setSelectedIndex(indiceTemporal);
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });

        //Agregando el deslizador del texto de la derecha
        SPBarajas = new JScrollPane();
        this.add(SPBarajas);

        //Botón para borrar baraja
        BEstudiar = new JButton("Estudiar");
        BEstudiar.setBorder(new LineBorder(Color.GRAY, 2));
        BEstudiar.setBackground(colorTexto);
        //Evento al picar el botón de estudiar
        BEstudiar.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                VentanaEstudio VE = new VentanaEstudio("", LListaBarajas.getSelectedValue().toString());
                setVisible(false);

                VE.addWindowListener(new WindowAdapter() {

                    @Override
                    public void windowClosing(WindowEvent e) {
                        //System.out.println("Se cerró la ventana de estudio");
                        int indiceTemporal = LListaBarajas.getSelectedIndex();
                        LListaBarajas.clearSelection();
                        LListaBarajas.setSelectedIndex(indiceTemporal);
                        setVisible(true);
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {

                    }

                });
            }
        });
        this.add(BEstudiar);

        //Botón de Borrar directorio inmediatamente abajo
        BBorrar = new JButton("Borrar baraja");
        BBorrar.setBorder(new LineBorder(Color.GRAY, 2));
        BBorrar.setBackground(colorTexto);
        //Evento para borrar el archivo desde la carpeta, quizá se puede omitir el .txt
        BBorrar.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                //System.out.println("Clic borrar dir");

                String pregunta = "¿Seguro que quiere borrar?";
                String confirmación = "Sí";
                String negación = "No";
                if(Files.exists(Paths.get(Paths.get("").toAbsolutePath().toString()+"/lingua.txt"))) {
                    pregunta = Herr.leerLineaArchivo("lingua.txt", 10, numColumnaIdioma);
                    confirmación = Herr.leerLineaArchivo("lingua.txt", 11, numColumnaIdioma);
                    negación = Herr.leerLineaArchivo("lingua.txt", 12, numColumnaIdioma);
                }

                //Ventanita para preguntar si uno quiere en verdad borrar la carpeta
                int result = JOptionPane.showConfirmDialog(BBorrar,
                        pregunta,confirmación+" - "+negación,
                        JOptionPane.YES_NO_OPTION,JOptionPane.QUESTION_MESSAGE);

                Exception ex = new Exception();
                ex.printStackTrace();

                if(result == JOptionPane.YES_OPTION){
                    String dir = dirVoc+"/"+LListaBarajas.getSelectedValue().toString().trim();
                    File arch = new File(dir);
                    System.out.println("Se borró el archivo "+arch.getName());
                    arch.delete();

                    dir = dirHist+"/"+LListaBarajas.getSelectedValue().toString().trim();
                    arch = new File(dir);
                    System.out.println("Se borró el archivo "+arch.getName());
                    arch.delete();
                }
                try{
                    LListaBarajas.setListData(new File(dirVoc+"/").list());
                }catch (NullPointerException g){
                    System.out.println("Ignorar este error, ocurre tras borrar carpetas");
                }
            }
        });
        this.add(BBorrar);

        //Botón para cambiar a modo obscuro
        BModoObscuro = new JButton("Modo Obscuro");
        BModoObscuro.setBorder(new LineBorder(Color.GRAY, 2));
        BModoObscuro.setBackground(colorTexto);
        //Evento al dar clic al modo oscuro
        BModoObscuro.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                try{
                    //System.out.println("Cambiando modo de color");
                    if(Integer.parseInt(Herr.leerLineaArchivo("config.txt",0,0)) == 0) {
                        Herr.cambiarArchivoUnaLinea("config.txt", 0, "1");
                        System.out.println("Se cambió a modo obscuro");
                    }
                    else if(Integer.parseInt(Herr.leerLineaArchivo("config.txt",0,0)) == 1){
                        Herr.cambiarArchivoUnaLinea("config.txt",0, "0");
                        System.out.println("Se cambió a modo claro");
                    }
                    FormatoConfig();
                }
                catch (IOException g){
                    System.out.println("Error al cambiar modo oscuro");
                }
            }
        });
        this.add(BModoObscuro);

        //Para editar el archivo de texto de la baraja
        BEditar = new JButton("Editar");
        BEditar.setBackground(colorTexto);
        BEditar.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try{
                    Desktop.getDesktop().open(new File(dirVoc+"/"+LListaBarajas.getSelectedValue().toString()));
                    Desktop.getDesktop().open(new File(dirRecs+"/"+LListaBarajas.getSelectedValue().toString()));

                    /*f(System.getProperty("os.name").contains("Windows")){
                        Runtime.getRuntime().exec("explorer.exe /select," + Paths.get(dirRecs+"/"+LListaBarajas.getSelectedValue().toString()));
                    }
                    /*if(System.getProperty("os.name").contains("Mac")){
                        Runtime.getRuntime().exec()
                    }*/

                }catch (IOException f){
                    System.err.println("Error al abrir el archivo de edición");
                }
            }
        });
        this.add(BEditar);

        //Botón para reiniciar archivo de veces estudiadas
        BReiniciar = new JButton("Reiniciar");
        BReiniciar.setBorder(new LineBorder(Color.GRAY, 2));
        BReiniciar.setBackground(colorTexto);
        //Evento al dar clic al reiniciar
        BReiniciar.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                Herr.fabricarArchivo00(dirVoc+"/"+LListaBarajas.getSelectedValue().toString(),
                                    dirHist+"/"+LListaBarajas.getSelectedValue().toString());
                try{
                    String textoTemp = new String(Files.readAllBytes(Paths.get(dirHist+"/"+LListaBarajas.getSelectedValue().toString())), StandardCharsets.UTF_16LE);
                    TBListaEstudiadas.setText(textoTemp);
                }catch(IOException f){
                    System.err.println("Error al reiniciar el archivo de historial");
                }

            }
        });
        this.add(BReiniciar);

        BAjustes = new JButton("Ajustes");
        BAjustes.setBackground(colorFondo);
        BAjustes.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                VentanaAjustes VA = new VentanaAjustes();
                VA.setLocationRelativeTo(null);

                VA.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosing(WindowEvent e) {
                        //System.out.println("Se cerró la ventana de Ajustes");
                    }

                    @Override
                    public void windowClosed(WindowEvent e) {
                        FormatoConfig();
                        Formato();
                        int temp = LListaBarajas.getSelectedIndex();
                        LListaBarajas.clearSelection();
                        LListaBarajas.setSelectedIndex(temp);

                    }
                });
            }
        });
        this.add(BAjustes);

        this.setDropTarget(new DropTarget() {
            public synchronized void drop(DropTargetDropEvent e) {
                try {
                    e.acceptDrop(1);
                    java.util.List<File> droppedFiles = (List)e.getTransferable().getTransferData(DataFlavor.javaFileListFlavor);
                    Iterator var3 = droppedFiles.iterator();

                    while(var3.hasNext()) {
                        File file = (File)var3.next();
                        System.out.println("Importando archivo:" + file.getName());
                        //
                        Files.copy(file.toPath(),Paths.get(dirVoc+"/"+file.getName()), REPLACE_EXISTING);

                        if(Herr.tieneFormatoImportacion(dirVoc+"/"+file.getName())){
                            System.out.println("Formato de importación correcto");
                        }
                        else{
                            System.out.println("Formato de importación incorrecto");
                            Herr.corregirFormatoImportacion(dirVoc+"/"+file.getName());
                        }
                        Herr.fabricarArchivo00(dirVoc+"/"+file.getName(),  dirHist+"/"+file.getName());

                        if(Files.exists(Paths.get(dirRecs+"/"+file.getName()))){
                            System.out.println("Se encontraron archivos en la carpeta de recursos, bórrelos manualmente");
                        }else{
                            Files.createDirectory(Paths.get(dirRecs+"/"+file.getName()));
                        }

                    }
                    LListaBarajas.setListData(new File(dirVoc).list());

                } catch (Exception var5) {
                    var5.printStackTrace();
                    System.out.println("Error al importar archivo");
                }
                FormatoConfig();
                Formato();
            }
        });

        this.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent componentEvent) {
                ancho = getWidth();
                alto = getHeight();
                Formato();
                try {
                    Herr.cambiarArchivoUnaLinea("config.txt", 4, ancho + "\t" + alto);
                }
                catch(IOException e){
                    System.err.println("No se pudo guardar el tamaño preferido");
                }
                if(LListaBarajas.getSelectedValue() != null){
                    LListaBarajas.clearSelection();
                    TBListaPalabras.setText("");
                    TBListaEstudiadas.setText("");
                }
            }
        });

    }

    private void Formato(){

        TBListaPalabras.setBounds(6*ancho/24, 2*alto/24, 12*ancho/24, 18*alto/24);
        SPPalabras.setBounds(6*ancho/24, 2*alto/24, 12*ancho/24, 18*alto/24);
        //TBListaEstudiadas.setBounds(37*ancho/48, 2*alto/24, 10*ancho/48 - 10, 18*alto/24);
        SPEstudiadas.setBounds(37*ancho/48, 2*alto/24, 10*ancho/48 -10, 18*alto/24);
        LBarajas.setBounds(ancho/48, alto/48, 10*ancho/24, 3*alto/48);
        LInfoBaraja.setBounds(6*ancho/24, alto/48, 10*ancho/24, 3*alto/48);
        LVecesEstudiadas.setBounds(37*ancho/48, alto/48, 10*ancho/24, 3*alto/48);
        //LListaBarajas.setBounds(ancho/48, 2*alto/24, 5*ancho/24, 18*alto/24);
        LListaBarajas.setLayoutOrientation(JList.VERTICAL);
        SPBarajas.setBounds(ancho/48, 2*alto/24, 5*ancho/24, 18*alto/24);
        BBorrar.setBounds(3*ancho/24, 20*alto/24, 2*ancho/24, 2*alto/24);
        BBorrar.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        BEstudiar.setBounds(ancho/24, 20*alto/24, 2*ancho/24, 2*alto/24);
        BEstudiar.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        BReiniciar.setBounds(40*ancho/48, 20*alto/24, 2*ancho/24, 2*alto/24);
        BReiniciar.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        BModoObscuro.setBounds(20*ancho/48, 20*alto/24, 2*ancho/24, 2*alto/24);
        BModoObscuro.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        BEditar.setBounds(24*ancho/48, 20*alto/24, 2*ancho/24, 2*alto/24);
        BEditar.setFont(new Font(fuente, Font.PLAIN, ancho/90));
        BAjustes.setBounds(32*ancho/48, 20*alto/24, 2*ancho/24, 2*alto/24);
        BAjustes.setFont(new Font(fuente, Font.PLAIN, ancho/90));

        try{
            if(new File(dirVoc).list().length > 0){
                SPBarajas.setViewportView(LListaBarajas);
                SPPalabras.setViewportView(TBListaPalabras);
                SPEstudiadas.setViewportView(TBListaEstudiadas);
            }
        }catch (NullPointerException n) {
            n.printStackTrace();
        }

    }

    private void FormatoConfig(){
        System.out.println("Aplicando cambios de formato");

        //Luego vemos si vale la pena cambiar también el color de los botones para que sea diferente al de los fondos

        fuente = Herr.leerLineaArchivo("config.txt",6,0);
        int tamFuenteChica = Integer.parseInt(Herr.leerLineaArchivo("config.txt",8,1));
        int tamFuenteGrande = Integer.parseInt(Herr.leerLineaArchivo("config.txt",8,0));

        TBListaPalabras.setFont(new Font(fuente, Font.PLAIN, tamFuenteChica));
        TBListaEstudiadas.setFont(new Font(fuente, Font.PLAIN, tamFuenteChica));
        LBarajas.setFont(new Font(fuente, Font.PLAIN, tamFuenteGrande));
        LInfoBaraja.setFont(new Font(fuente, Font.PLAIN, tamFuenteGrande));
        LVecesEstudiadas.setFont(new Font(fuente, Font.PLAIN, tamFuenteGrande));
        LListaBarajas.setFont(new Font(fuente, Font.PLAIN, tamFuenteChica+5));

        int oscuro = Integer.parseInt(Herr.leerLineaArchivo("config.txt",0,0));
        //Si oscuro está prendido (1) entonces al restar pasa a la línea de texto 2, que es la que corresponde con config modoObscuro
        colorFondo = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt",3-oscuro, 0),16));
        colorTexto = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt",3-oscuro, 1),16));
        Color colorMarcos = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 3 - oscuro, 2), 16));
        Color colorFondoTexto = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 3 - oscuro, 3), 16));
        Color colorTextoCuadros = new Color(Integer.parseInt(Herr.leerLineaArchivo("config.txt", 3 - oscuro, 4), 16));

        getContentPane().setBackground(colorFondo);
        LVecesEstudiadas.setForeground(colorTexto);
        LInfoBaraja.setForeground(colorTexto);
        LBarajas.setForeground(colorTexto);
        LListaBarajas.setBackground(colorFondoTexto);
        LListaBarajas.setForeground(colorTextoCuadros);
        SPBarajas.setBorder(new LineBorder(colorMarcos, 3));
        TBListaPalabras.setBackground(colorFondoTexto);
        TBListaPalabras.setForeground(colorTextoCuadros);
        TBListaPalabras.setBorder(new LineBorder(colorMarcos,3));
        SPPalabras.setBorder(new LineBorder(colorMarcos, 3));
        TBListaEstudiadas.setBackground(colorFondoTexto);
        TBListaEstudiadas.setForeground(colorTextoCuadros);
        SPEstudiadas.setBorder(new LineBorder(colorMarcos, 3));
        BEstudiar.setForeground(colorTexto);
        BEstudiar.setBorder(new LineBorder(colorMarcos, 3));
        BBorrar.setForeground(colorTexto);
        BBorrar.setBorder(new LineBorder(colorMarcos, 3));
        BModoObscuro.setForeground(colorTexto);
        BModoObscuro.setBorder(new LineBorder(colorMarcos, 3));
        BReiniciar.setForeground(colorTexto);
        BReiniciar.setBorder(new LineBorder(colorMarcos, 3));
        BEditar.setForeground(colorTexto);
        BEditar.setBorder(new LineBorder(colorMarcos, 3));
        BAjustes.setForeground(colorTexto);
        BAjustes.setBorder(new LineBorder(colorMarcos,3));
        //this.update(this.getGraphics());

        if(Files.exists(Paths.get(Paths.get("").toAbsolutePath().toString()+"/lingua.txt"))){
            System.out.println("Se encontró archivo lingua.txt");
            String idioma = Herr.leerLineaArchivo("config.txt",1);
            String listaIdiomas = Herr.leerLineaArchivo("lingua.txt", 0);
            numColumnaIdioma = Herr.numColumna(idioma, listaIdiomas);

            LBarajas.setText(Herr.leerLineaArchivo("lingua.txt",1, numColumnaIdioma));
            LInfoBaraja.setText(Herr.leerLineaArchivo("lingua.txt",2, numColumnaIdioma));
            LVecesEstudiadas.setText(Herr.leerLineaArchivo("lingua.txt",3, numColumnaIdioma));
            BEstudiar.setText(Herr.leerLineaArchivo("lingua.txt",4, numColumnaIdioma));
            BBorrar.setText(Herr.leerLineaArchivo("lingua.txt",5, numColumnaIdioma));
            BModoObscuro.setText(Herr.leerLineaArchivo("lingua.txt",6, numColumnaIdioma));
            BEditar.setText(Herr.leerLineaArchivo("lingua.txt",7, numColumnaIdioma));
            BAjustes.setText(Herr.leerLineaArchivo("lingua.txt",8, numColumnaIdioma));
            BReiniciar.setText(Herr.leerLineaArchivo("lingua.txt",9, numColumnaIdioma));
        }
    }

    private void Inicializar(){

        //Dado que necesita que exista las carpetas /voc /hist y /recs, la voy a crear al inicio para ahorrar errores después
        //Esto es durante el proceso de inicialización

        dirVoc = Paths.get("").toAbsolutePath().toString() + "/voc";
        dirHist = Paths.get("").toAbsolutePath().toString() + "/hist";
        dirRecs = Paths.get("").toAbsolutePath().toString() + "/recs";

        try {
            if(!Files.exists(Paths.get(dirVoc))) {
                Files.createDirectories(Paths.get(dirVoc));
                System.out.println("Generando archivo voc");
            }
            if(!Files.exists(Paths.get(dirHist))) {
                Files.createDirectories(Paths.get(dirHist));
                System.out.println("Generando archivo hist");
            }
            if(!Files.exists(Paths.get(dirRecs))) {
                Files.createDirectories(Paths.get(dirRecs));
                System.out.println("Generando archivo recs");
            }
            if(!Files.exists(Paths.get("config.txt"))) {
                System.out.println("Generando archivo config");
                File archConfig = new File("config.txt");

                archConfig.createNewFile();
                String[] lineasArreglo = {
                        "0",                          //Modo oscuro apagado
                        "en",                         //Idioma predeterminado
                        //(fondo-letras-marcos-fondoTexto-textoListas)
                        "001F33\tFAFAFA\t6495ED\t324B62\tFAFAFA",     //Colores de tema oscuro predeterminado
                        "f7fcff\t000000\t6495ED\tFFFFFF\t000000",     //Colores de tema claro
                        "1200\t600",                                     //Dimensiones predeterminadas VentanaPrincipal
                        "1200\t600",                                    //Dimensiones ventana Estudio
                        "Yu Gothic UI",                                     //Fuente predeterminada
                        "80ffb5\tff80ca\tff808a",                           //Colores de TFRespuesta (verde, morado, rojo)
                        "30\t15",                              //Tamaño de fuente grande, tamaño de fuente chica
                        "4ae2fe\tffa046\tff5746\t5b81fe\t46fe6f",                                  //Colores marquito de acuerdo al modoEstudio (Ilimitados), el primero le toca a Fonético, el segundo a traducción etc...
                        "-20",               //volumen predeterminado
                        "1",                //veces que se cicla el audio
                        "1"};               //si es distinto de cero, hay azar en el primer ordenamiento
                List<String> lineasConfig = Arrays.asList(lineasArreglo);
                Herr.cambiarArchivo("config.txt", lineasConfig);
            }
        } catch (IOException e) {
            System.out.println("Error al crear la carpeta 'voc'");
        }

        ancho = Integer.parseInt(Herr.leerLineaArchivo("config.txt",4,0));
        alto = Integer.parseInt(Herr.leerLineaArchivo("config.txt",4,1));
        fuente = Herr.leerLineaArchivo("config.txt",6,0).trim();

        setSize(new Dimension(ancho, alto));
        setTitle("Jalabra");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(800, 480));

    }
}
