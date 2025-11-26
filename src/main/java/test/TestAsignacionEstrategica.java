package test;

import modelo.*;
import FS.SistemaArchivos;
import Persistencia.PersistenciaJSON;

/**
 * Prueba para verificar que la asignación estratégica de bloques funciona correctamente
 */
public class TestAsignacionEstrategica {

    public static void main(String[] args) {
        System.out.println("=== PRUEBA DE ASIGNACIÓN ESTRATÉGICA ===\n");

        // Crear disco de 100 bloques
        SD disco = new SD(100);
        SistemaArchivos fs = new SistemaArchivos(disco);

        System.out.println("Disco creado: " + disco.getTamanoTotal() + " bloques");
        System.out.println("Bloques libres: " + disco.getBloquesLibres() + "\n");

        // Cargar el demo de planificadores
        System.out.println("Cargando demo_planificadores.json...\n");
        PersistenciaJSON.cargarEstado(fs, disco, "demo_planificadores.json");

        System.out.println("\n=== VERIFICACIÓN DE BLOQUES ASIGNADOS ===\n");

        // Verificar algunos archivos clave
        verificarArchivo(disco, "archivo_10.dat", 10);
        verificarArchivo(disco, "archivo_20.dat", 20);
        verificarArchivo(disco, "archivo_30.dat", 30);
        verificarArchivo(disco, "archivo_50.dat", 50);
        verificarArchivo(disco, "archivo_70.dat", 70);
        verificarArchivo(disco, "archivo_90.dat", 90);

        System.out.println("\n=== VERIFICACIÓN DE ARCHIVOS GRANDES ===\n");
        verificarArchivoGrande(disco, "inicio.txt", 1, 2);
        verificarArchivoGrande(disco, "documento_grande.pdf", 5, 7);
        verificarArchivoGrande(disco, "base_datos.db", 15, 10);
        verificarArchivoGrande(disco, "video_demo.mp4", 35, 12);

        // Mostrar estadísticas
        System.out.println("\n=== ESTADÍSTICAS FINALES ===");
        System.out.println("Bloques ocupados: " + (disco.getTamanoTotal() - disco.getBloquesLibres()));
        System.out.println("Bloques libres: " + disco.getBloquesLibres());
        System.out.println("Uso del disco: " + String.format("%.1f%%",
            (1.0 - (double)disco.getBloquesLibres()/disco.getTamanoTotal()) * 100));

        System.out.println("\n=== MAPA VISUAL DEL DISCO (primeros 100 bloques) ===");
        mostrarMapaDisco(disco);
    }

    private static void verificarArchivo(SD disco, String nombreArchivo, int bloqueEsperado) {
        Bloque[] bloques = disco.getBloques();

        // Buscar el archivo en el disco
        for (int i = 0; i < bloques.length; i++) {
            Bloque b = bloques[i];
            if (!b.isEstaLibre() && nombreArchivo.equals(b.getArchivoPropietario())) {
                int diferencia = Math.abs(i - bloqueEsperado);
                String status = diferencia <= 5 ? "✓ CORRECTO" : "⚠ REVISAR";
                System.out.println(String.format("%-20s → Esperado: ~%-3d | Asignado: %-3d | Diferencia: %-2d %s",
                    nombreArchivo, bloqueEsperado, i, diferencia, status));
                return;
            }
        }
        System.out.println(String.format("%-20s → ✗ NO ENCONTRADO", nombreArchivo));
    }

    private static void verificarArchivoGrande(SD disco, String nombreArchivo, int bloqueInicio, int tamano) {
        Bloque[] bloques = disco.getBloques();

        // Buscar primer bloque del archivo
        int primerBloque = -1;
        for (int i = 0; i < bloques.length; i++) {
            Bloque b = bloques[i];
            if (!b.isEstaLibre() && nombreArchivo.equals(b.getArchivoPropietario())) {
                primerBloque = i;
                break;
            }
        }

        if (primerBloque == -1) {
            System.out.println(String.format("%-25s → ✗ NO ENCONTRADO", nombreArchivo));
            return;
        }

        // Contar bloques en la cadena
        int conteo = 0;
        int actual = primerBloque;
        StringBuilder cadena = new StringBuilder();

        while (actual != -1 && conteo < 20) { // límite de seguridad
            cadena.append(actual);
            conteo++;
            actual = bloques[actual].getSiguienteBloque();
            if (actual != -1) cadena.append("→");
        }

        int diferencia = Math.abs(primerBloque - bloqueInicio);
        String status = diferencia <= 5 ? "✓" : "⚠";

        System.out.println(String.format("%-25s → Inicio esperado: ~%-2d | Asignado: %-3d | Bloques: %d/%d %s",
            nombreArchivo, bloqueInicio, primerBloque, conteo, tamano, status));
        System.out.println("  Cadena: " + cadena.toString());
    }

    private static void mostrarMapaDisco(SD disco) {
        Bloque[] bloques = disco.getBloques();

        System.out.println("\nLeyenda: [##] = ocupado | [  ] = libre | Número = índice del bloque\n");

        for (int i = 0; i < Math.min(100, bloques.length); i++) {
            if (i % 10 == 0) {
                System.out.print(String.format("\nBloques %2d-%2d: ", i, Math.min(i+9, bloques.length-1)));
            }

            Bloque b = bloques[i];
            if (b.isEstaLibre()) {
                System.out.print("[  ]");
            } else {
                System.out.print("[##]");
            }
        }
        System.out.println("\n");
    }
}
