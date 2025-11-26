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
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.JMenuBar;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileNameExtensionFilter;
import Persistencia.PersistenciaJSON; 
import Persistencia.PersistenciaCSV;




/**
 * 
 * @author Diego A. Vivolo / Gabriel 
 */
public class Ventana_Principal extends javax.swing.JFrame {

    private SO simulador;
    private boolean modoAdministrador = true;
    private Timer timerActualizacion;
    

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
    private JLabel labelEstadisticasCache;
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
    private JButton btnEstadisticas;

    // Controles de velocidad
    private JSlider sliderVelocidad;
    private JLabel labelVelocidad;

    private static final int NUM_BLOQUES = 100;
    private static final int CICLO_MS = 500;

    public Ventana_Principal() {
        initComponents();
        setTitle("Simulador de Sistema de Archivos - SO 2425-2");
        setSize(1300, 950);
        setLocationRelativeTo(null);
        
        setJMenuBar(crearBarraMenu());
        

        simulador = new SO(NUM_BLOQUES, CICLO_MS);
        

        simulador.iniciar();
        

        iniciarTimerGUI();
    }
    
    private void initComponents() {
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        

        JPanel panelPrincipal = new JPanel(new BorderLayout(5, 5));
        panelPrincipal.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        

        JPanel panelControles = crearPanelControles();
        panelPrincipal.add(panelControles, BorderLayout.NORTH);
        

        JSplitPane splitCentral = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitCentral.setDividerLocation(200);
        

        JPanel panelArbol = crearPanelArbol();
        splitCentral.setLeftComponent(panelArbol);
        

        JTabbedPane pestanas = crearPestanas();
        splitCentral.setRightComponent(pestanas);
        
        panelPrincipal.add(splitCentral, BorderLayout.CENTER);
        

        JSplitPane splitInferior = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitInferior.setDividerLocation(600);
        splitInferior.setPreferredSize(new Dimension(0, 200));
        

        JPanel panelLog = crearPanelLog();
        splitInferior.setLeftComponent(panelLog);
        

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
        

        panel.add(new JLabel("Planificador:"));
        comboPlanificador = new JComboBox<>(new String[]{"FIFO", "SSTF", "SCAN", "C-SCAN"});
        comboPlanificador.addActionListener(e -> cambiarPlanificador());
        panel.add(comboPlanificador);
        
        panel.add(new JSeparator(SwingConstants.VERTICAL));

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
        
        btnEstadisticas = new JButton("Estadísticas");
        btnEstadisticas.addActionListener(e -> mostrarEstadisticas());
        panel.add(btnEstadisticas); 
    
        panel.add(new JSeparator(SwingConstants.VERTICAL));

        btnPausar = new JButton("Pausar");
        btnPausar.addActionListener(e -> togglePausa());
        panel.add(btnPausar);

        panel.add(new JSeparator(SwingConstants.VERTICAL));

        // Control de velocidad mejorado
        JPanel panelVelocidad = new JPanel();
        panelVelocidad.setLayout(new BoxLayout(panelVelocidad, BoxLayout.Y_AXIS));
        panelVelocidad.setBorder(BorderFactory.createTitledBorder("Velocidad"));

        // Panel superior con botones
        JPanel panelBotones = new JPanel(new FlowLayout(FlowLayout.CENTER, 3, 0));

        JButton btnRapido = new JButton("x4");
        btnRapido.setToolTipText("Muy rápido (100 ms)");
        btnRapido.setPreferredSize(new Dimension(50, 25));
        btnRapido.setFont(new Font("Arial", Font.BOLD, 10));
        btnRapido.setBackground(new Color(255, 200, 200));
        btnRapido.setFocusPainted(false);
        btnRapido.addActionListener(e -> establecerVelocidad(100));
        panelBotones.add(btnRapido);

        JButton btnNormal = new JButton("x2");
        btnNormal.setToolTipText("Normal (300 ms)");
        btnNormal.setPreferredSize(new Dimension(50, 25));
        btnNormal.setFont(new Font("Arial", Font.BOLD, 10));
        btnNormal.setBackground(new Color(255, 240, 200));
        btnNormal.setFocusPainted(false);
        btnNormal.addActionListener(e -> establecerVelocidad(300));
        panelBotones.add(btnNormal);

        JButton btnLento = new JButton("x1");
        btnLento.setToolTipText("Lento (500 ms)");
        btnLento.setPreferredSize(new Dimension(50, 25));
        btnLento.setFont(new Font("Arial", Font.BOLD, 10));
        btnLento.setBackground(new Color(200, 255, 200));
        btnLento.setFocusPainted(false);
        btnLento.addActionListener(e -> establecerVelocidad(500));
        panelBotones.add(btnLento);

        JButton btnMuyLento = new JButton("x0.5");
        btnMuyLento.setToolTipText("Muy lento (1000 ms)");
        btnMuyLento.setPreferredSize(new Dimension(50, 25));
        btnMuyLento.setFont(new Font("Arial", Font.BOLD, 10));
        btnMuyLento.setBackground(new Color(200, 220, 255));
        btnMuyLento.setFocusPainted(false);
        btnMuyLento.addActionListener(e -> establecerVelocidad(1000));
        panelBotones.add(btnMuyLento);

        panelVelocidad.add(panelBotones);

        // Panel inferior con slider y label
        JPanel panelSlider = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 2));

        sliderVelocidad = new JSlider(50, 2000, CICLO_MS);
        sliderVelocidad.setMajorTickSpacing(500);
        sliderVelocidad.setMinorTickSpacing(100);
        sliderVelocidad.setPaintTicks(true);
        sliderVelocidad.setPreferredSize(new Dimension(180, 30));
        sliderVelocidad.addChangeListener(e -> cambiarVelocidadSimulacion());
        panelSlider.add(sliderVelocidad);

        labelVelocidad = new JLabel(CICLO_MS + " ms (Lento)");
        labelVelocidad.setPreferredSize(new Dimension(100, 20));
        labelVelocidad.setFont(new Font("Arial", Font.PLAIN, 11));
        labelVelocidad.setHorizontalAlignment(SwingConstants.CENTER);
        panelSlider.add(labelVelocidad);

        panelVelocidad.add(panelSlider);

        panel.add(panelVelocidad);
        panel.add(new JSeparator(SwingConstants.VERTICAL));

        labelCiclo = new JLabel("Ciclo: 0");
        panel.add(labelCiclo);

        labelCabeza = new JLabel("Cabeza: 0");
        panel.add(labelCabeza);

        return panel;
    }
    
    private JMenuBar crearBarraMenu() {
        JMenuBar menuBar = new JMenuBar();


        JMenu menuArchivo = new JMenu("Archivo");


        JMenuItem itemGuardar = new JMenuItem("Guardar Estado del Sistema (.json)");
        itemGuardar.addActionListener(e -> accionGuardarEstado());


        JMenuItem itemCargar = new JMenuItem("Cargar Estado del Sistema (.json)");
        itemCargar.addActionListener(e -> accionCargarEstado());

        menuArchivo.add(itemGuardar);
        menuArchivo.add(itemCargar);
        menuArchivo.addSeparator();

        JMenuItem itemSalir = new JMenuItem("Salir");
        itemSalir.addActionListener(e -> System.exit(0));
        menuArchivo.add(itemSalir);


        JMenu menuReportes = new JMenu("Reportes");


        JMenuItem itemResumen = new JMenuItem("Exportar Resumen del Sistema (.txt)");
        itemResumen.addActionListener(e -> accionExportarResumen());


        JMenuItem itemEstadisticas = new JMenuItem("Exportar Estadísticas Procesos (.csv)");
        itemEstadisticas.addActionListener(e -> accionExportarCSV());

        menuReportes.add(itemResumen);
        menuReportes.add(itemEstadisticas);


        menuBar.add(menuArchivo);
        menuBar.add(menuReportes);

        return menuBar;
        }
    
    private void accionGuardarEstado() {
    JFileChooser fc = new JFileChooser();
    fc.setDialogTitle("Guardar estado del sistema");
    fc.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));
    
    if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
        File archivo = fc.getSelectedFile();
        String ruta = archivo.getAbsolutePath();
        if (!ruta.toLowerCase().endsWith(".json")) {
            ruta += ".json";
        }
        

        PersistenciaJSON.guardarEstado(
            simulador.getSistemaArchivos(), 
            simulador.getDisco(), 
            ruta
        );
        JOptionPane.showMessageDialog(this, "Estado guardado exitosamente.");
    }
}

    private void accionCargarEstado() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Cargar estado del sistema");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos JSON", "json"));

        if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();


            PersistenciaJSON.cargarEstado(
                simulador.getSistemaArchivos(), 
                simulador.getDisco(), 
                archivo.getAbsolutePath()
            );


            actualizarArbol();
            panelDisco.repaint();
            actualizarTablaAsignacion();
            JOptionPane.showMessageDialog(this, "Estado cargado (Revisar consola para detalles).");
        }
    }

    private void accionExportarResumen() {
        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar Resumen");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos de Texto", "txt"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();
            String ruta = archivo.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".txt")) {
                ruta += ".txt";
            }

            PersistenciaJSON.exportarResumen(
                simulador.getSistemaArchivos(),
                simulador.getDisco(),
                ruta
            );
            JOptionPane.showMessageDialog(this, "Resumen exportado.");
        }
    }

    private void accionExportarCSV() {


        JFileChooser fc = new JFileChooser();
        fc.setDialogTitle("Exportar Estadísticas");
        fc.setFileFilter(new FileNameExtensionFilter("Archivos CSV", "csv"));

        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File archivo = fc.getSelectedFile();
            String ruta = archivo.getAbsolutePath();
            if (!ruta.toLowerCase().endsWith(".csv")) {
                ruta += ".csv";
            }

            try {

                 PersistenciaJSON.guardarResultadosProcesos(simulador.getColaTerminados(), ruta);

                JOptionPane.showMessageDialog(this, "Estadísticas exportadas.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Error al exportar: " + ex.getMessage());
            }
        }
    }   

    
    
    private JPanel crearPanelArbol() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder("Sistema de Archivos"));
        

        DefaultMutableTreeNode raiz = new DefaultMutableTreeNode("raiz");
        modeloArbol = new DefaultTreeModel(raiz);
        arbolArchivos = new JTree(modeloArbol);
        arbolArchivos.setRootVisible(true);
        arbolArchivos.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        

        arbolArchivos.addTreeSelectionListener(e -> {
            TreePath path = arbolArchivos.getSelectionPath();
            if (path != null) {
                DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) path.getLastPathComponent();

            }
        });

        // Agregar listener para doble click
        arbolArchivos.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    TreePath path = arbolArchivos.getPathForLocation(e.getX(), e.getY());
                    if (path != null) {
                        DefaultMutableTreeNode nodo = (DefaultMutableTreeNode) path.getLastPathComponent();
                        Object obj = nodo.getUserObject();

                        if (obj instanceof Archivo) {
                            Archivo arch = (Archivo) obj;
                            // Tanto administrador como usuario pueden leer archivos
                            simulador.leerArchivoDirecto(arch);
                        } else if (obj instanceof Directorio) {
                            // Doble click en directorio: expandir/colapsar
                            if (arbolArchivos.isExpanded(path)) {
                                arbolArchivos.collapsePath(path);
                            } else {
                                arbolArchivos.expandPath(path);
                            }
                        }
                    }
                }
            }
        });
        
        JScrollPane scrollArbol = new JScrollPane(arbolArchivos);
        panel.add(scrollArbol, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JTabbedPane crearPestanas() {
        JTabbedPane pestanas = new JTabbedPane();
        

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
        

        JPanel panelCache = new JPanel(new BorderLayout());
        panelCache.setBorder(BorderFactory.createTitledBorder("Buffer Cache (FIFO)"));
        modeloListaCache = new DefaultListModel<>();
        listaCache = new JList<>(modeloListaCache);
        JScrollPane scrollCache = new JScrollPane(listaCache);
        panelCache.add(scrollCache, BorderLayout.CENTER);

        // Panel de estadísticas del cache
        JPanel panelEstadisticas = new JPanel();
        panelEstadisticas.setLayout(new BoxLayout(panelEstadisticas, BoxLayout.Y_AXIS));
        panelEstadisticas.setBorder(BorderFactory.createTitledBorder("Estadísticas de Rendimiento"));
        labelEstadisticasCache = new JLabel("Hits: 0 | Misses: 0 | Tasa: 0%");
        panelEstadisticas.add(labelEstadisticasCache);
        panelEstadisticas.add(new JLabel("Máximo: 10 bloques"));

        panelCache.add(panelEstadisticas, BorderLayout.SOUTH);
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
    

    
    private void dibujarDisco(Graphics g) {
        if (simulador == null) return;
        
        SD disco = simulador.getDisco();
        Bloque[] bloques = disco.getBloques();
        int total = bloques.length;
        

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
        

        java.util.Map<String, Color> coloresArchivos = obtenerColoresArchivos();
        int cabeza = simulador.getCabezaDisco();
        
        for (int i = 0; i < total; i++) {
            int fila = i / cols;
            int col = i % cols;
            int x = margen + col * (tamBloque + espaciado);
            int y = margen + fila * (tamBloque + espaciado);
            
            Bloque bloque = bloques[i];
            

            Color colorBloque;
            if (bloque.isEstaLibre()) {
                colorBloque = new Color(200, 200, 200); // Gris para libre
            } else {
                String propietario = bloque.getArchivoPropietario();
                colorBloque = coloresArchivos.getOrDefault(propietario, new Color(100, 149, 237));
            }
            

            g2d.setColor(colorBloque);
            g2d.fillRoundRect(x, y, tamBloque, tamBloque, 8, 8);
            
            // Borde
            g2d.setColor(Color.DARK_GRAY);
            g2d.drawRoundRect(x, y, tamBloque, tamBloque, 8, 8);
            

            if (i == cabeza) {
                g2d.setColor(Color.RED);
                g2d.setStroke(new BasicStroke(3));
                g2d.drawRoundRect(x - 2, y - 2, tamBloque + 4, tamBloque + 4, 10, 10);
                g2d.setStroke(new BasicStroke(1));
            }
            

            g2d.setColor(bloque.isEstaLibre() ? Color.DARK_GRAY : Color.WHITE);
            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            String num = String.valueOf(i);
            FontMetrics fm = g2d.getFontMetrics();
            int xTexto = x + (tamBloque - fm.stringWidth(num)) / 2;
            int yTexto = y + (tamBloque + fm.getAscent()) / 2 - 2;
            g2d.drawString(num, xTexto, yTexto);
            

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
    

    
    private void iniciarTimerGUI() {
        timerActualizacion = new Timer(200, e -> actualizarGUI());
        timerActualizacion.start();
    }
    
    private void actualizarGUI() {
        if (simulador == null) return;
        
        SwingUtilities.invokeLater(() -> {

            actualizarArbol();
            

            panelDisco.repaint();
            

            actualizarTablaAsignacion();
            

            String log = simulador.getLog();
            if (!log.equals(areaLog.getText())) {
                areaLog.setText(log);
                areaLog.setCaretPosition(areaLog.getDocument().getLength());
            }
            

            actualizarColaProcesos();
            

            actualizarCache();
            

            labelCiclo.setText("Ciclo: " + simulador.getReloj().getCicloActual());
            labelCabeza.setText("Cabeza: " + simulador.getCabezaDisco());
            labelEstadoDisco.setText("Bloques libres: " + simulador.getDisco().getBloquesLibres() 
                    + "/" + simulador.getDisco().getTamanoTotal());
        });
    }
    
    private void actualizarArbol() {

            TreePath pathSeleccionado = arbolArchivos.getSelectionPath();
            String rutaString = null;
            if (pathSeleccionado != null) {
          
                rutaString = pathSeleccionado.toString(); 
            }


            DefaultMutableTreeNode raizNodo = (DefaultMutableTreeNode) modeloArbol.getRoot();
            raizNodo.removeAllChildren();

            Directorio raizDir = simulador.getSistemaArchivos().getDirectorioRaiz();
            construirArbolRecursivo(raizNodo, raizDir);

            modeloArbol.reload();
            expandirTodoArbol(); 

     
            if (rutaString != null) {
                restaurarSeleccion(rutaString);
            }
        }

     
        private void restaurarSeleccion(String rutaGuardada) {

            for (int i = 0; i < arbolArchivos.getRowCount(); i++) {
                TreePath path = arbolArchivos.getPathForRow(i);
           
                if (path.toString().equals(rutaGuardada)) {
                    arbolArchivos.setSelectionPath(path);
                    arbolArchivos.setSelectionRow(i);
                    return;
                }
            }
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
        
 
        modeloListaProcesos.addElement("=== LISTOS ===");
        for (PCB p : simulador.getColaListos()) {
            modeloListaProcesos.addElement("  " + p.getNombre() + " - " + p.getEstado());
        }
        

        modeloListaProcesos.addElement("=== EN CPU ===");
        PCB enCpu = simulador.getCpu().getProcesoActual();
        if (enCpu != null) {
            modeloListaProcesos.addElement("  " + enCpu.getNombre() + " - EJECUCION");
        }
        

        modeloListaProcesos.addElement("=== BLOQUEADOS ===");
        for (PCB p : simulador.getColaBloqueados()) {
            modeloListaProcesos.addElement("  " + p.getNombre() + " - " + p.getEstado());
        }
        

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

        // Actualizar estadísticas de rendimiento
        labelEstadisticasCache.setText(cache.getEstadisticas());
    }
    
    
    private void cambiarModo(boolean esAdmin) {
        modoAdministrador = esAdmin;

        // Modo Usuario: SOLO LECTURA (todos los botones deshabilitados excepto leer)
        // Modo Administrador: TODAS LAS OPERACIONES
        btnCrearArchivo.setEnabled(esAdmin);
        btnCrearDirectorio.setEnabled(esAdmin);
        btnEliminar.setEnabled(esAdmin);
        btnRenombrar.setEnabled(esAdmin);

        if (btnEstadisticas != null) {
            btnEstadisticas.setEnabled(esAdmin);
        }

        // El botón leer está siempre habilitado (no hay btnLeer, se hace con doble click)
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
        // Solo el administrador puede crear archivos
        if (!modoAdministrador) {
            JOptionPane.showMessageDialog(this, "Operacion no permitida en modo Usuario.\nModo Usuario: SOLO LECTURA",
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

            // Administrador crea archivos con propietario "admin"
            simulador.crearArchivoDesdeGUI(nombre.trim(), tamano, "admin");

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
    
    private void mostrarEstadisticas() {
        if (!modoAdministrador) return;

        String reporte = simulador.obtenerEstadisticasGlobales();

        JTextArea areaStats = new JTextArea(reporte);
        areaStats.setEditable(false);
        areaStats.setFont(new Font("Monospaced", Font.PLAIN, 12));

        JOptionPane.showMessageDialog(this, new JScrollPane(areaStats), 
                "Reporte del Sistema (Admin)", JOptionPane.INFORMATION_MESSAGE);
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

            // Tanto administrador como usuario pueden leer TODOS los archivos
            // Modo Usuario = Solo lectura (puede leer todo)
            // Modo Administrador = Todas las operaciones (incluyendo leer todo)
            simulador.leerArchivoDirecto(arch);
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

    private void cambiarVelocidadSimulacion() {
        if (simulador == null) return;

        int nuevaVelocidad = sliderVelocidad.getValue();
        simulador.cambiarVelocidad(nuevaVelocidad);

        // Actualizar label con texto descriptivo
        String textoVelocidad;
        if (nuevaVelocidad <= 100) {
            textoVelocidad = nuevaVelocidad + " ms (Rápido)";
        } else if (nuevaVelocidad <= 500) {
            textoVelocidad = nuevaVelocidad + " ms (Normal)";
        } else if (nuevaVelocidad <= 1000) {
            textoVelocidad = nuevaVelocidad + " ms (Lento)";
        } else {
            textoVelocidad = nuevaVelocidad + " ms (Muy Lento)";
        }
        labelVelocidad.setText(textoVelocidad);
    }

    private void establecerVelocidad(int velocidadMs) {
        if (simulador == null) return;
        sliderVelocidad.setValue(velocidadMs);
        // El cambio del slider automáticamente llamará a cambiarVelocidadSimulacion()
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

        }
        
        java.awt.EventQueue.invokeLater(() -> {
            new Ventana_Principal().setVisible(true);
        });
    }
}

