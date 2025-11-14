package FS;

import FS.SistemaArchivos;
import modelo.SD;

/**
 *
 * @author Diego A. Vivolo
 */
public class EstadoSistema {

    public EstadoSistema() { } 


    public SistemaArchivos sistemaArchivos;
    public SD disco;


    public EstadoSistema(SistemaArchivos sa, SD sd) {
        this.sistemaArchivos = sa;
        this.disco = sd;
    }
}