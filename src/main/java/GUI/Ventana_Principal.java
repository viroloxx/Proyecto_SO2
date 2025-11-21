package GUI;

import sistema.SO;
import modelo.*;
import FS.SistemaArchivos;
import FS.BufferCache;
import estructura_datos.ListaEnlazada;
import estructura_datos.Cola;
import planificacion.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Ventana Principal del Simulador de Sistema de Archivos
 * @author Diego A. Vivolo / Gabriel (completado)
 */
public class Ventana_Principal extends javax.swing.JFrame {

    private SO simulador;
    private boolean modoAdministrador = true;
    private Timer timerActualizacion;
    
    // Componentes de la GUI
    private JTree arbolArchivos;
    private DefaultTreeModel modeloArbol;
    private JPanel panelDisco;
    private JTable tablaAsignacion;
    private DefaultTableModel modeloTabla;
    private JTextArea areaLog;
    private JList<String> listaProcesos;
    private DefaultListModel<String> modeloListaProcesos;
    private JList<String> listaCache;
    private DefaultListModel<String> modeloListaCache;
    private JLabel labelEstadoDisco;
    private JLabel labelCiclo;
    private JLabel labelCabeza;
    
    // Botones y controles
    private JRadioButton rbUsuario;
    private JRadioButton rbAdministrador;
    private JComboBox<String> comboPlanificador;
    private JButton btnCrearArchivo;
    private JButton btnCrearDirectorio;
    private JButton btnEliminar;
    private JButton btnRenombrar;
    private JButton btnLeer;
    private JButton btnPausar;
    
    // Constantes
    private static final int NUM_BLOQUES = 100;
    private static final int CICLO_MS = 500;

    public Ventana_Principal() {
        initComponents();
        setTitle("Simulador de Sistema de Archivos - SO 2425-2");
        setSize(1200, 850);
        setLocationRelativeTo(null);
        
        // Inicializar el simulador
        simulador = new SO(NUM_BLOQUES, CICLO_MS);
        
        // Iniciar simulación
        simulador.iniciar();
        
        // Iniciar timer de actualización de GUI
        iniciarTimerGUI();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        
        // Panel principal con BorderLayout
        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // ========== PANEL SUPERIOR (Controles) ==========
        JPanel panelControles = crearPanelControles();
        panelPrincipal.add(panelControles, BorderLayout.NORTH);
        
        // ========== PANEL CENTRAL (JTree + Pestañas) ==========
        JSplitPane splitCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitCentral.setDividerLocation(200);
        
        // Panel izquierdo: JTree
        JPanel panelArbol = crearPanelArbol();
        splitCentral.setLeftComponent(panelArbol);
        
        // Panel derecho: Pestañas
        JTabbedPane pestanas = crearPestanas();
        splitCentral.setRightComponent(pestanas);
        
        panelPrincipal.add(splitCentral, BorderLayout.CENTER);
        
        // ========== PANEL INFERIOR (Log + Procesos) ==========
        JSplitPane splitInferior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitInferior.setDividerLocation(600);
        splitInferior.setPreferredSize(new Dimension(0, 200));
        
        // Log de eventos
        JPanel panelLog = crearPanelLog();
        splitInferior.setLeftComponent(panelLog);
        
        // Cola de procesos
        JPanel panelProcesos = crearPanelProcesos();
        splitInferior.setRightComponent(panelProcesos);
        
        panelPrincipal.add(splitInferior, BorderLayout.SOUTH);
        
        setContentPane(panelPrincipal);
    }
    
    private JPanel crearPanelControles() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        panel.setBorder(BorderFactory.createTitledBorder("Controles"));
        
        // Modo de usuario
        JPanel panelModo = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        panelModo.add(new JLabel("Modo:"));
        ButtonGroup grupoModo = new ButtonGroup();
        rbAdministrador = new JRadioButton("Administrador", true);
        rbUsuario = new JRadioButton("Usuario");
        grupoModo.add(rbAdministrador);
        grupoModo.add(rbUsuario);
        panelModo.add(rbAdministrador);
        panelModo.add(rbUsuario);
        
        rbAdministrador.addActionListener(e -> cambiarModo(true));
        rbUsuario.addActionListener(e -> cambiarModo(false));
        
        panel.add(panelModo);
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Selector de planificador
        panel.add(new JLabel("Planificador:"));
        comboPlanificador = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        comboPlanificador.addActionListener(e -> cambiarPlanificador());
        panel.add(comboPlanificador);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Botones CRUD
        btnCrearArchivo = new JButton("Crear Archivo");
        btnCrearArchivo.addActionListener(e -> crearArchivo());
        panel.add(btnCrearArchivo);
        
        btnCrearDirectorio = new JButton("Crear Directorio");
        btnCrearDirectorio.addActionListener(e -> crearDirectorio());
        panel.add(btnCrearDirectorio);
        
        btnLeer = new JButton("Leer");
        btnLeer.addActionListener(e -> leerArchivo());
        panel.add(btnLeer);
        
        btnRenombrar = new JButton("Renombrar");
        btnRenombrar.addActionListener(e -> renombrar());
        panel.add(btnRenombrar);
        
        btnEliminar = new JButton("Eliminar");
        btnEliminar.addActionListener(e -> eliminar());
        panel.add(btnEliminar);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL));
        
        // Botón pausar/reanudar
        btnPausar = new JButton("Pausar");
        btnPausar.addActionListener(e -> togglePausa());
        panel.add(btnPausar);
        
        // Labels de estado
        labelCiclo = new JLabel("Ciclo: 0");
        panel.add(labelCiclo);
        
        labelCabeza = new JLabel("Cabeza: 0");
        panel.add(labelCabeza);
        
        return panel;
    }
    
    private JPanel crearPanelArbol() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sistema de Archivos"));
        
        // Crear el modelo del árbol
        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("raiz");
        modeloArbol = new DefaultTreeModel(raiz);
        arbolArchivos = new JTree(modeloArbol);
        arbolArchivos.setRootVisible(true);
        arbolArchivos.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        
        // Listener para selección
        arbolArchivos.addTreeSelectionListener(e -> {
            TreePath path = arbolArchivos.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) path.getLastPathComponent();
                // Se puede usar para mostrar info adicional
            }
        });
        
        JScrollPane scrollArbol = new JScrollPane(arbolArchivos);
        panel.add(scrollArbol, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTabbedPane crearPestanas() {
        JTabbedPane pestanas = new JTabbedPane();
        
        // Pestaña 1: Simulación de Disco
        JPanel panelSimDisco = new JPanel(new BorderLayout());
        panelSimDisco.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        
        panelDisco = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                dibujarDisco(g);
            }
        };
        panelDisco.setPreferredSize(new Dimension(500, 400));
        panelDisco.setBackground(Color.WHITE);
        
        JScrollPane scrollDisco = new JScrollPane(panelDisco);
        panelSimDisco.add(scrollDisco, BorderLayout.CENTER);
        
        labelEstadoDisco = new JLabel("Bloques libres: " + NUM_BLOQUES + "/" + NUM_BLOQUES);
        panelSimDisco.add(labelEstadoDisco, BorderLayout.SOUTH);
        
        pestanas.addTab("Simulación de Disco", panelSimDisco);
        
        // Pestaña 2: Tabla de Asignación
        JPanel panelTabla = new JPanel(new BorderLayout());
        String[] columnas = {"Archivo", "Bloques", "Primer Bloque", "Color", "Propietario"};
        modeloTabla = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaAsignacion = new JTable(modeloTabla);
        tablaAsignacion.setRowHeight(25);
        
        // Renderer para la columna de color
        tablaAsignacion.getColumnModel().getColumn(3).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                JLabel label = new JLabel();
                label.setOpaque(true);
                if (value instanceof Color) {
                    label.setBackground((Color) value);
                }
                return label;
            }
        });
        
        JScrollPane scrollTabla = new JScrollPane(tablaAsignacion);
        panelTabla.add(scrollTabla, BorderLayout.CENTER);
        pestanas.addTab("Tabla de Asignación", panelTabla);
        
        // Pestaña 3: Cache/Buffer
        JPanel panelCache = new JPanel(new BorderLayout());
        panelCache.setBorder(BorderFactory.createTitledBorder("Buffer Cache (FIFO)"));
        modeloListaCache = new DefaultListModel<>();
        listaCache = new JList<>(modeloListaCache);
        JScrollPane scrollCache = new JScrollPane(listaCache);
        panelCache.add(scrollCache, BorderLayout.CENTER);
        panelCache.add(new JLabel("Máximo: 10 bloques"), BorderLayout.SOUTH);
        pestanas.addTab("Cache", panelCache);
        
        return pestanas;
    }
    
    private JPanel crearPanelLog() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Log de Eventos"));
        
        areaLog = new JTextArea();
        areaLog.setEditable(false);
        areaLog.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        JScrollPane scrollLog = new JScrollPane(areaLog);
        scrollLog.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
        panel.add(scrollLog, BorderLayout.CENTER);
        
        JButton btnLimpiarLog = new JButton("Limpiar");
        btnLimpiarLog.addActionListener(e -> {
            areaLog.setText("");
            simulador.limpiarLog();
        });
        panel.add(btnLimpiarLog, BorderLayout.SOUTH);
        
        return panel;
    }
    
    private JPanel crearPanelProcesos() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Cola de Procesos"));
        
        modeloListaProcesos = new DefaultListModel<>();
        listaProcesos = new JList<>(modeloListaProcesos);
        listaProcesos.setFont(new Font("Monospaced", Font.PLAIN, 11));
        
        JScrollPane scrollProcesos = new JScrollPane(listaProcesos);
        panel.add(scrollProcesos, BorderLayout.CENTER);
        
        return panel;
    }
    
    // ========== MÉTODOS DE DIBUJO ==========
    
    private void dibujarDisco(Graphics g) {
        if (simulador == null) return;
        
        SD disco = simulador.getDisco();
        Bloque[] bloques = disco.getBloques();
        int total = bloques.length;
        
        // Configuración de dibujo
        int cols = 10;
        int filas = (int) Math.ceil((double) total / cols);
        int tamBloque = 40;
        int espaciado = 5;
        int margen = 20;
        
        // Ajustar tamaño del panel
        int anchoNecesario = cols * (tamBloque + espaciado) + margen * 2;
        int altoNecesario = filas * (tamBloque + espaciado) + margen * 2;
        panelDisco.setPreferredSize(new Dimension(anchoNecesario, altoNecesario));
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Obtener colores de archivos
        java.util.Map<String, Color> coloresArchivos = obtenerColoresArchivos();
        int cabeza = simulador.getCabezaDisco();
        
        for (int i = 0; i < total; i++) {
            int fila = i / cols;
            int col = i % cols;
            int x = margen + col * (tamBloque + espaciado);
            int y = margen + fila * (tamBloque + espaciado);
            
            Bloque bloque = bloques[i];
            
            // Determinar color
            Color colorBloque;
            if (bloque.isEstaLibre()) {
                colorBloque = new Color(200, 200, 200); // Gris para libre
            } else {
                String propietario = bloque.getArchivoPropietario();
                colorBloque = coloresArchivos.getOrDefault(propietario, new Color(100, 149, 237));
            }
            
            // Dibujar bloque
            g2d.setColor(colorBloque);
            g2d.fillRoundRect(x, y, tamBloque, tamBloque, 8, 8);
            
            // Borde
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRoundRect(x, y, tamBloque, tamBloque, 8, 8);
            
            // Resaltar cabeza del disco
            if (i == cabeza) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(x - 2, y - 2, tamBloque + 4, tamBloque + 4, 10, 10);
                g2d.setStroke(new BasicStroke(1));
            }
            
            // Número del bloque
            g2d.setColor(bloque.isEstaLibre() ? Color.DARK_GRAY : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String num = String.valueOf(i);
            FontMetrics fm = g2d.getFontMetrics();
            int xTexto = x + (tamBloque - fm.stringWidth(num)) / 2;
            int yTexto = y + (tamBloque + fm.getAscent()) / 2 - 2;
            g2d.drawString(num, xTexto, yTexto);
            
            // Mostrar siguiente bloque si está encadenado
            if (!bloque.isEstaLibre() && bloque.getSiguienteBloque() != -1) {
                g2d.setFont(new Font("Arial", Font.PLAIN, 8));
                g2d.setColor(Color.WHITE);
                String sig = "->" + bloque.getSiguienteBloque();
                g2d.drawString(sig, x + 2, y + tamBloque - 3);
            }
        }
        
        panelDisco.revalidate();
    }
    
    private java.util.Map<String, Color> obtenerColoresArchivos() {
        java.util.Map<String, Color> mapa = new java.util.HashMap<>();
        obtenerColoresRecursivo(simulador.getSistemaArchivos().getDirectorioRaiz(), mapa);
        return mapa;
    }
    
    private void obtenerColoresRecursivo(Directorio dir, java.util.Map<String, Color> mapa) {
        for (Object hijo : dir.getHijos()) {
            if (hijo instanceof Archivo) {
                Archivo arch = (Archivo) hijo;
                mapa.put(arch.getNombre(), arch.getColor());
            } else if (hijo instanceof Directorio) {
                obtenerColoresRecursivo((Directorio) hijo, mapa);
            }
        }
    }
    
    // ========== MÉTODOS DE ACTUALIZACIÓN ==========
    
    private void iniciarTimerGUI() {
        timerActualizacion = new Timer(200, e -> actualizarGUI());
        timerActualizacion.start();
    }
    
    private void actualizarGUI() {
        if (simulador == null) return;
        
        SwingUtilities.invokeLater(() -> {
            // Actualizar árbol
            actualizarArbol();
            
            // Actualizar disco
            panelDisco.repaint();
            
            // Actualizar tabla de asignación
            actualizarTablaAsignacion();
            
            // Actualizar log
            String log = simulador.getLog();
            if (!log.equals(areaLog.getText())) {
                areaLog.setText(log);
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
            }
            
            // Actualizar cola de procesos
            actualizarColaProcesos();
            
            // Actualizar cache
            actualizarCache();
            
            // Actualizar labels
            labelCiclo.setText("Ciclo: " + simulador.getReloj().getCicloActual());
            labelCabeza.setText("Cabeza: " + simulador.getCabezaDisco());
            labelEstadoDisco.setText("Bloques libres: " + simulador.getDisco().getBloquesLibres() 
                    + "/" + simulador.getDisco().getTamanoTotal());
        });
    }
    
    private void actualizarArbol() {
        DefaultMutableTreeNode raizNodo = (DefaultMutableTreeNode) modeloArbol.getRoot();
        raizNodo.removeAllChildren();
        
        Directorio raizDir = simulador.getSistemaArchivos().getDirectorioRaiz();
        construirArbolRecursivo(raizNodo, raizDir);
        
        modeloArbol.reload();
        expandirTodoArbol();
    }
    
    private void construirArbolRecursivo(DefaultMutableTreeNode nodoArbol, Directorio directorio) {
        for (Object hijo : directorio.getHijos()) {
            if (hijo instanceof Archivo) {
                Archivo arch = (Archivo) hijo;
                DefaultMutableTreeNode nodoArchivo = new DefaultMutableTreeNode(
                    arch.getNombre() + " [" + arch.getTamanoEnBloques() + " bloques]"
                );
                nodoArchivo.setUserObject(arch);
                nodoArbol.add(nodoArchivo);
            } else if (hijo instanceof Directorio) {
                Directorio subDir = (Directorio) hijo;
                DefaultMutableTreeNode nodoDir = new DefaultMutableTreeNode(subDir.getNombre());
                nodoDir.setUserObject(subDir);
                nodoArbol.add(nodoDir);
                construirArbolRecursivo(nodoDir, subDir);
            }
        }
    }
    
    private void expandirTodoArbol() {
        for (int i = 0; i < arbolArchivos.getRowCount(); i++) {
            arbolArchivos.expandRow(i);
        }
    }
    
    private void actualizarTablaAsignacion() {
        modeloTabla.setRowCount(0);
        agregarArchivosATabla(simulador.getSistemaArchivos().getDirectorioRaiz());
    }
    
    private void agregarArchivosATabla(Directorio dir) {
        for (Object hijo : dir.getHijos()) {
            if (hijo instanceof Archivo) {
                Archivo arch = (Archivo) hijo;
                modeloTabla.addRow(new Object[]{
                    arch.getNombre(),
                    arch.getTamanoEnBloques(),
                    arch.getDireccionPrimerBloque(),
                    arch.getColor(),
                    arch.getPropietario()
                });
            } else if (hijo instanceof Directorio) {
                agregarArchivosATabla((Directorio) hijo);
            }
        }
    }
    
    private void actualizarColaProcesos() {
        modeloListaProcesos.clear();
        
        // Procesos listos
        modeloListaProcesos.addElement("=== LISTOS ===");
        for (PCB p : simulador.getColaListos()) {
            modeloListaProcesos.addElement("  " + p.getNombre() + " - " + p.getEstado());
        }
        
        // Proceso en ejecución
        modeloListaProcesos.addElement("=== EN CPU ===");
        PCB enCpu = simulador.getCpu().getProcesoActual();
        if (enCpu != null) {
            modeloListaProcesos.addElement("  " + enCpu.getNombre() + " - EJECUCION");
        }
        
        // Procesos bloqueados
        modeloListaProcesos.addElement("=== BLOQUEADOS ===");
        for (PCB p : simulador.getColaBloqueados()) {
            modeloListaProcesos.addElement("  " + p.getNombre() + " - " + p.getEstado());
        }
        
        // Cola de I/O
        modeloListaProcesos.addElement("=== COLA I/O ===");
        for (SolicitudIO s : simulador.getColaSolicitudes()) {
            modeloListaProcesos.addElement("  " + s.toString());
        }
    }
    
    private void actualizarCache() {
        modeloListaCache.clear();
        BufferCache cache = simulador.getCacheDisco();
        for (BufferCache.EntradaCache entrada : cache.getCacheContenido()) {
            String info = "Bloque " + entrada.getIndiceBloque();
            Bloque b = entrada.getBloque();
            if (!b.isEstaLibre()) {
                info += " (" + b.getArchivoPropietario() + ")";
            }
            modeloListaCache.addElement(info);
        }
        if (modeloListaCache.isEmpty()) {
            modeloListaCache.addElement("(Cache vacio)");
        }
    }
    
    // ========== MÉTODOS DE ACCIÓN ==========
    
    private void cambiarModo(boolean esAdmin) {
        modoAdministrador = esAdmin;
        btnCrearArchivo.setEnabled(esAdmin);
        btnCrearDirectorio.setEnabled(esAdmin);
        btnEliminar.setEnabled(esAdmin);
        btnRenombrar.setEnabled(esAdmin);
        // Leer siempre está habilitado
    }
    
    private void cambiarPlanificador() {
        String seleccion = (String) comboPlanificador.getSelectedItem();
        Planificador nuevo;
        switch (seleccion) {
            case "SSTF":
                nuevo = new SSTF();
                break;
            case "SCAN":
                nuevo = new SCAN();
                break;
            case "C-SCAN":
                nuevo = new CSCAN();
                break;
            default:
                nuevo = new FIFO();
        }
        simulador.cambiarPlanificador(nuevo);
    }
    
    private void crearArchivo() {
        if (!modoAdministrador) {
            JOptionPane.showMessageDialog(this, "Operacion no permitida en modo Usuario", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombre = JOptionPane.showInputDialog(this, "Nombre del archivo:");
        if (nombre == null || nombre.trim().isEmpty()) return;
        
        String tamanoStr = JOptionPane.showInputDialog(this, "Tamano en bloques (1-20):");
        if (tamanoStr == null) return;
        
        try {
            int tamano = Integer.parseInt(tamanoStr);
            if (tamano < 1 || tamano > 20) {
                JOptionPane.showMessageDialog(this, "El tamano debe estar entre 1 y 20", 
                    "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }
            simulador.crearArchivoDesdeGUI(nombre.trim(), tamano);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "Tamano invalido", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void crearDirectorio() {
        if (!modoAdministrador) {
            JOptionPane.showMessageDialog(this, "Operacion no permitida en modo Usuario", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        String nombre = JOptionPane.showInputDialog(this, "Nombre del directorio:");
        if (nombre != null && !nombre.trim().isEmpty()) {
            simulador.crearDirectorioDesdeGUI(nombre.trim());
        }
    }
    
    private void leerArchivo() {
        TreePath path = arbolArchivos.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un archivo del arbol", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object obj = nodo.getUserObject();
        
        if (obj instanceof Archivo) {
            Archivo arch = (Archivo) obj;
            simulador.leerArchivoDesdeGUI(arch.getNombre());
        } else {
            JOptionPane.showMessageDialog(this, "Solo se pueden leer archivos", 
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void renombrar() {
        if (!modoAdministrador) {
            JOptionPane.showMessageDialog(this, "Operacion no permitida en modo Usuario", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        TreePath path = arbolArchivos.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un elemento del arbol", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object obj = nodo.getUserObject();
        String nombreActual = null;
        
        if (obj instanceof Archivo) {
            nombreActual = ((Archivo) obj).getNombre();
        } else if (obj instanceof Directorio) {
            nombreActual = ((Directorio) obj).getNombre();
        }
        
        if (nombreActual != null && !nombreActual.equals("raiz")) {
            String nuevoNombre = JOptionPane.showInputDialog(this, 
                "Nuevo nombre para '" + nombreActual + "':");
            if (nuevoNombre != null && !nuevoNombre.trim().isEmpty()) {
                simulador.renombrarDesdeGUI(nombreActual, nuevoNombre.trim());
            }
        }
    }
    
    private void eliminar() {
        if (!modoAdministrador) {
            JOptionPane.showMessageDialog(this, "Operacion no permitida en modo Usuario", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        TreePath path = arbolArchivos.getSelectionPath();
        if (path == null) {
            JOptionPane.showMessageDialog(this, "Seleccione un elemento del arbol", 
                "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) path.getLastPathComponent();
        Object obj = nodo.getUserObject();
        
        if (obj instanceof Archivo) {
            Archivo arch = (Archivo) obj;
            int confirm = JOptionPane.showConfirmDialog(this, 
                "Eliminar archivo '" + arch.getNombre() + "'?",
                "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
            if (confirm == JOptionPane.YES_OPTION) {
                simulador.eliminarDesdeGUI(arch.getNombre(), true);
            }
        } else if (obj instanceof Directorio) {
            Directorio dir = (Directorio) obj;
            if (!dir.getNombre().equals("raiz")) {
                int confirm = JOptionPane.showConfirmDialog(this, 
                    "Eliminar directorio '" + dir.getNombre() + "' y todo su contenido?",
                    "Confirmar eliminacion", JOptionPane.YES_NO_OPTION);
                if (confirm == JOptionPane.YES_OPTION) {
                    simulador.eliminarDesdeGUI(dir.getNombre(), false);
                }
            }
        }
    }
    
    private void togglePausa() {
        if (simulador.getReloj().estaPausado()) {
            simulador.reanudar();
            btnPausar.setText("Pausar");
        } else {
            simulador.pausar();
            btnPausar.setText("Reanudar");
        }
    }

    public static void main(String args[]) {
        try {
            for (UIManager.LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception e) {
            // Usar look and feel por defecto
        }
        
        java.awt.EventQueue.invokeLater(() -> {
            new Ventana_Principal().setVisible(true);
        });
    }
}
