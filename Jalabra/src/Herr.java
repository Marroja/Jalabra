/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Marroja
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public abstract class Herr {

    public static boolean tieneFormatoImportacion(String dirArchivoBaraja){
        String primeraLinea = leerLineaArchivo(dirArchivoBaraja,0);
        return primeraLinea.contains("%0") && primeraLinea.contains("!0");
    }

    public static void fabricarArchivo00(String dirArchivoBaraja, String dirArchivoHistBaraja){
        //Recibe la dirección idéntica del archivo de la baraja pero con /hist/baraja, no /voc/baraja
        try{
            if(!Files.exists(Paths.get(dirArchivoHistBaraja))){
                System.out.println("Creando archivo /hist/baraja");
                Files.createFile(Paths.get(dirArchivoHistBaraja));
            }
            BufferedReader lector = Files.newBufferedReader(Paths.get(dirArchivoBaraja),StandardCharsets.UTF_16LE);
            lector.readLine();                                                                   //Para brincar la primera línea
            lector.readLine();
            String linea;

            List<String> lineas00 = new ArrayList<String>();
            while((linea = lector.readLine()) != null){
                String palabraSolita = linea.split("\\t")[0];
                lineas00.add(palabraSolita+":"+""+(Integer.parseInt(leerLineaArchivo("config.txt",12))*(int)(Math.floor(5*Math.random())))); //Este se puede cambiar a 0, 1, 2, 3... en Config.
                System.out.println("Agregando: "+palabraSolita);
            }
            cambiarArchivo(dirArchivoHistBaraja, lineas00);

            lector.close();
        }catch (IOException e){
            System.out.println("Hubo un error al crear el archivo "+dirArchivoHistBaraja);
        }
    }

    public static void cambiarArchivoUnaLinea(String direccionArchivo, int indice, String textoCambio) throws IOException{
        Path dirArchivo = Paths.get(direccionArchivo);
        List<String> lineas = Files.readAllLines(dirArchivo, StandardCharsets.UTF_16LE);
        lineas.set(indice, textoCambio);
        cambiarArchivo(direccionArchivo, lineas);
    }

    public static void cambiarArchivo(String direccionArchivo, List<String> todoTexto) throws IOException{
        Path dirNuevoArchivo = Paths.get(direccionArchivo);
        Files.write(dirNuevoArchivo, todoTexto, StandardCharsets.UTF_16LE);
        //System.out.println("Se cambió el archivo "+direccionArchivo);
    }

    public static String leerLineaArchivo(String direccionArchivo, int indiceLinea, int indiceTabular){

        String salida = "";
        try{
            BufferedReader lector = Files.newBufferedReader(Paths.get(direccionArchivo),StandardCharsets.UTF_16LE);

            for(int i = 0; i < indiceLinea; i++){ //Para brincar todas las líneas hasta antes de "indice linea"
                   lector.readLine();
            }

            //System.out.println("Leyendo archivo:"+direccionArchivo);
            String linea = lector.readLine();         //Este te regresa toda la línea, hay que ver qué tab regresar

            salida = linea.split("\\t")[indiceTabular];

        } catch(IOException e){
            System.err.println("Error al extraer la línea ("+indiceLinea+") en el archivo "+direccionArchivo);
        }
        //System.out.println("Se extrajo "+salida+" del archivo");
        return salida.replace("\uFEFF","");
    }

    public static String leerLineaArchivo(String direccionArchivo, int indiceLinea){

        String linea = "";
        try{
            BufferedReader lector = Files.newBufferedReader(Paths.get(direccionArchivo),StandardCharsets.UTF_16LE);

            for(int i = 0; i < indiceLinea; i++){ //Para brincar todas las líneas hasta antes de "indice linea"
                lector.readLine();
            }

            linea = lector.readLine();         //Este te regresa toda la línea, hay que ver qué tab regresar

        } catch(IOException e){
            System.err.println("Error al extraer la línea ("+indiceLinea+") en el archivo "+direccionArchivo);
        }
        //System.out.println("Se extrajo "+linea+" del archivo");
        return linea.replace("\uFEFF","");
    }

    public static String[] dividirConBandera(String textoADividir, String bandera){
        String[] textoDividido = textoADividir.split(bandera);
        return textoDividido;
    }

    public static String[] siguientePreguntaRespuesta(String direccionArchivo, String direccionArchivoHistorial, int columnaPregunta, int columnaRespuesta){

        int indice = indicePalabraMenosEstudiada(direccionArchivoHistorial);

        String[] preguntaRespuesta = new String[2];                                                           //!0 !2 .etc || Español Kanji || +2 (palabra etc)
        preguntaRespuesta[0] = leerLineaArchivo(direccionArchivo, indice + 2, columnaPregunta);     //Este +2 es porque el +1 es para las etiquetas de columna
        preguntaRespuesta[1] = leerLineaArchivo(direccionArchivo, indice + 2, columnaRespuesta);    //Y el +0 es para indicar quién es pregunta y quién respuesta

        return preguntaRespuesta;
    }

    public static void actualizarHistorial(String direccionArchivoHistorial, int indice){
        String palabraConNumero = leerLineaArchivo(direccionArchivoHistorial, indice);
        palabraConNumero = palabraConNumero.split(":")[0]+":"+(int)(Integer.parseInt(palabraConNumero.split(":")[1])+(int)(5+Math.floor(5*Math.random())));
        cambiarLineaArchivo(direccionArchivoHistorial, indice, palabraConNumero);
    }

    public static String palabraMenosEstudiada(String direccionArchivo){
        String[] arregloPalabras = new String[0]; // palabra:12
        try{
            List<String> todoTextoArchivo = Files.readAllLines(Paths.get(direccionArchivo),StandardCharsets.UTF_16LE);
            arregloPalabras = todoTextoArchivo.toArray(new String[todoTextoArchivo.size()]);
        }catch (IOException e){
            System.err.println("Error leyendo el archivo de historial de la baraja");
        }
        int[] veces = new int[arregloPalabras.length];
        for(int i =0; i< arregloPalabras.length; i++){
            veces[i] = Integer.parseInt(arregloPalabras[i].split(":")[1]);
        }
        return arregloPalabras[FindSmallest(veces)].split(":")[0];
    }

    public static int indicePalabraMenosEstudiada(String direccionArchivo){
        String[] arregloPalabras = new String[0]; // palabra:12
        try{
            List<String> todoTextoArchivo = Files.readAllLines(Paths.get(direccionArchivo),StandardCharsets.UTF_16LE);
            arregloPalabras = todoTextoArchivo.toArray(new String[todoTextoArchivo.size()]);
        }catch (IOException e){
            System.err.println("Error leyendo el archivo de historial de la baraja");
        }
        int[] veces = new int[arregloPalabras.length];
        for(int i =0; i< arregloPalabras.length; i++){
            veces[i] = Integer.parseInt(arregloPalabras[i].split(":")[1]);
        }
        return FindSmallest(veces);
    }

    public static int FindSmallest (int [] arr1) {
        int index = 0;
        int min = arr1[index];

        for (int i=1; i<arr1.length; i++) {

            if (arr1[i] < min) {
                min = arr1[i];
                index = i;
            }
        }
        return index;
    }

    public static void cambiarLineaArchivo(String direccionArchivo, int indiceLinea, String textoAPoner){
        try{
            List<String> todoTextoArchivo = Files.readAllLines(Paths.get(direccionArchivo),StandardCharsets.UTF_16LE);
            todoTextoArchivo.set(indiceLinea, textoAPoner);
            cambiarArchivo(direccionArchivo, todoTextoArchivo);
        }catch (IOException e){
            System.err.println("Error al modificar el archivo de historial");
        }

    }

    public static int numColumna(String bandera, String texto){
        String antesBandera = texto.split(bandera)[0];
        return (int)antesBandera.chars().filter(ch -> ch == '\t').count();
    }

    public static void corregirFormatoImportacion(String dirArchivoBaraja) {

    }
}

