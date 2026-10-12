import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SwingMain extends JFrame {
    private final WaterNetwork network = createNetwork();
    private final WaterAllocationPolicy policy = new DefaultWaterAllocationPolicy();
    private final List<WaterRequest> requests = new ArrayList<>();
    private final List<String> allocationHistory = new ArrayList<>();
    private final List<String> eventHistory = new ArrayList<>();
    private final Queue<MaintenanceRequest> maintenanceQueue = new ArrayDeque<>();
    private final List<MaintenanceRequest> maintenanceHistory = new ArrayList<>();
    private final AtomicInteger requestSequence = new AtomicInteger(1);
    private final AtomicInteger maintenanceSequence = new AtomicInteger(1);
    private final MySqlPersistence persistence = new MySqlPersistence();

    private final Color ink = new Color(34, 48, 52);
    private final Color muted = new Color(103, 119, 122);
    private final Color teal = new Color(35, 119, 111);
    private final Color bg = new Color(242, 246, 245);
    private final DefaultTableModel reservoirModel = model("ID", "Reservoir", "Capacity (L)", "Remaining (L)");
    private final DefaultTableModel areaModel = model("ID", "Demand area", "Priority", "Daily demand (L)", "Reachable", "Unmet (L)");
    private final DefaultTableModel pipelineModel = model("ID", "From", "To", "Status");
    private final DefaultTableModel requestModel = model("Request", "Area", "Mode", "Requested (L)", "Allocated (L)", "Unmet (L)", "Status");
    private final DefaultTableModel maintenanceModel = model("Ticket", "Pipeline", "Issue", "Created", "Status");
    private final DefaultTableModel historyModel = model("Time", "Event / allocation details");
    private JLabel supplyMetric, allocatedMetric, unmetMetric, failedMetric, messageLabel;
    private JTable reservoirTable, areaTable, pipelineTable, requestTable, maintenanceTable, historyTable;
    private JComboBox<String> presetEventCombo;
    private JComboBox<String> manualAreaCombo, pipelineCombo, reservoirCombo, supplyReservoirCombo, priorityModeCombo;
    private JTextField litresField, supplyField;
    private JTextArea pathArea;
    private NetworkMapPanel networkMapPanel;

    public SwingMain() {
        super("Neer Nagar | Smart Water Distribution");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1280, 820);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        getContentPane().setBackground(bg);
        add(buildHeader(), BorderLayout.NORTH);
        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(new Font("SansSerif", Font.BOLD, 13));
        tabs.addTab("Dashboard", dashboardPage());
        tabs.addTab("Network Map", networkMapPage());
        tabs.addTab("Allocate Water", allocationPage());
        tabs.addTab("Network & Pipelines", networkPage());
        tabs.addTab("Events & Supply", eventsPage());
        tabs.addTab("History & Maintenance", historyPage());
        add(tabs, BorderLayout.CENTER);
        messageLabel = new JLabel("Ready. Select an action to begin.");
        messageLabel.setBorder(new EmptyBorder(8, 18, 10, 18));
        messageLabel.setForeground(muted);
        add(messageLabel, BorderLayout.SOUTH);
        persistence.initializeAndLoad(network, requests, allocationHistory, eventHistory, maintenanceQueue, maintenanceHistory);
        requestSequence.set(nextSequence(requests.stream().map(WaterRequest::getId).toArray(String[]::new), "WR-"));
        maintenanceSequence.set(nextSequence(maintenanceHistory.stream().map(MaintenanceRequest::getId).toArray(String[]::new), "MT-"));
        refreshAll();
        if (persistence.isEnabled()) message(persistence.getStatus());
    }

    private static DefaultTableModel model(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int col) { return false; }
        };
    }

    private JPanel buildHeader() {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(new Color(37, 55, 59));
        p.setBorder(new EmptyBorder(18, 24, 18, 24));
        JLabel title = new JLabel("<html><span style='font-size:21px;color:#ffffff;font-weight:bold'>NEER NAGAR</span><br><span style='font-size:12px;color:#b6d4d0'>SMART WATER DISTRIBUTION MANAGEMENT · JAVA SWING</span></html>");
        p.add(title, BorderLayout.WEST);
        JLabel badge = new JLabel("  LIVE SIMULATION  ");
        badge.setOpaque(true); badge.setBackground(new Color(58, 91, 87));
        badge.setForeground(Color.WHITE); badge.setBorder(new EmptyBorder(8, 10, 8, 10));
        p.add(badge, BorderLayout.EAST);
        return p;
    }

    private JPanel page() {
        JPanel p = new JPanel(new BorderLayout(14, 14));
        p.setBackground(bg);
        p.setBorder(new EmptyBorder(18, 20, 18, 20));
        return p;
    }

    private JPanel card(String title, String subtitle) {
        JPanel p = new JPanel(new BorderLayout(8, 10));
        p.setBackground(Color.WHITE);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 231, 229)),
                new EmptyBorder(16, 17, 16, 17)));
        JPanel heading = new JPanel(new BorderLayout(0, 4));
        heading.setOpaque(false);
        JLabel h = new JLabel(title); h.setFont(new Font("SansSerif", Font.BOLD, 16)); h.setForeground(ink);
        JLabel s = new JLabel(subtitle); s.setFont(new Font("SansSerif", Font.PLAIN, 11)); s.setForeground(muted);
        heading.add(h, BorderLayout.NORTH); heading.add(s, BorderLayout.SOUTH);
        p.add(heading, BorderLayout.NORTH);
        return p;
    }

    private JButton btn(String text, boolean primary) {
        JButton b = new JButton(text);
        b.setFocusPainted(false);
        b.setFont(new Font("SansSerif", Font.BOLD, 12));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorder(new EmptyBorder(9, 13, 9, 13));
        b.setBackground(primary ? teal : new Color(232, 240, 238));
        b.setForeground(primary ? Color.WHITE : ink);
        return b;
    }

    private JLabel metric(String value, String label) {
        JLabel l = new JLabel("<html><div style='font-size:11px;color:#687a7c'>" + label
                + "</div><div style='font-size:23px;font-weight:bold;color:#223034'>" + value + "</div></html>");
        l.setBorder(new EmptyBorder(14, 16, 14, 10));
        l.setOpaque(true); l.setBackground(Color.WHITE);
        l.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(222, 231, 229)),
                new EmptyBorder(14, 16, 14, 10)));
        return l;
    }

    private JPanel dashboardPage() {
        JPanel p = page();
        JPanel metrics = new JPanel(new GridLayout(1, 4, 12, 0)); metrics.setOpaque(false);
        supplyMetric = metric("18,000 L", "SUPPLY REMAINING");
        allocatedMetric = metric("0 L", "ALLOCATED THIS SESSION");
        unmetMetric = metric("0 L", "UNMET REQUESTS");
        failedMetric = metric("0", "FAILED PIPELINES");
        metrics.add(supplyMetric); metrics.add(allocatedMetric); metrics.add(unmetMetric); metrics.add(failedMetric);
        p.add(metrics, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 3, 12, 0)); lower.setOpaque(false);
        JPanel summary = card("Current system snapshot", "Computed from the Java model");
        JTextArea text = new JTextArea();
        text.setName("dashboardSummary"); text.setEditable(false); text.setOpaque(false);
        text.setFont(new Font("SansSerif", Font.PLAIN, 13)); text.setForeground(ink);
        text.setLineWrap(true); text.setWrapStyleWord(true); text.setRows(8);
        JScrollPane summaryScroll = new JScrollPane(text);
        summaryScroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        summaryScroll.setBorder(BorderFactory.createEmptyBorder());
        summary.add(summaryScroll, BorderLayout.CENTER);
        lower.add(summary);

        JPanel balances = card("Reservoir balances", "Remaining supply refreshes after every allocation or supply event");
        reservoirTable = new JTable(reservoirModel); styleTable(reservoirTable);
        balances.add(new JScrollPane(reservoirTable), BorderLayout.CENTER);
        lower.add(balances);

        JPanel quick = card("Review demo sequence", "Use these actions to demonstrate the core requirements");
        JPanel actions = new JPanel(new GridLayout(0, 1, 0, 8)); actions.setOpaque(false);
        JButton b1 = btn("1. Allocate 2,500 L to Government Hospital", true);
        b1.addActionListener(e -> { performAllocation("A3", 2500, true, "Priority/manual demo"); refreshAll(); });
        JButton b2 = btn("2. Fail pipeline P9 (disconnects A9/A10)", false);
        b2.addActionListener(e -> failSelectedOr("P9"));
        JButton b3 = btn("3. Repair pipeline P9 and retry pending demand", false);
        b3.addActionListener(e -> repairSelectedOr("P9"));
        JButton b4 = btn("4. Run priority-based batch allocation", false);
        b4.addActionListener(e -> runBatch(true));
        actions.add(b1); actions.add(b2); actions.add(b3); actions.add(b4);
        quick.add(actions, BorderLayout.CENTER);
        lower.add(quick);
        p.add(lower, BorderLayout.CENTER);
        return p;
    }

    private JPanel networkMapPage() {
        JPanel p = page();
        JPanel mapCard = card("Live pipeline connectivity map", "Green/teal links are working; failed links turn red and display a break symbol. The map refreshes after every fault or repair.");
        networkMapPanel = new NetworkMapPanel();
        mapCard.add(networkMapPanel, BorderLayout.CENTER);
        JPanel legend = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 4));
        legend.setOpaque(false);
        legend.add(new JLabel("● Reservoir"));
        legend.add(new JLabel("● Normal demand area"));
        legend.add(new JLabel("● Critical institution"));
        JLabel failedLegend = new JLabel("━ × Failed pipeline");
        failedLegend.setForeground(new Color(190, 42, 48));
        legend.add(failedLegend);
        mapCard.add(legend, BorderLayout.SOUTH);
        p.add(mapCard, BorderLayout.CENTER);
        return p;
    }

    private JPanel allocationPage() {
        JPanel p = page();
        JPanel top = card("Manual area allocation", "Choose the demand area and quantity; the system selects a reachable reservoir automatically");
        JPanel form = new JPanel(new GridBagLayout()); form.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 5, 5, 5); c.anchor = GridBagConstraints.WEST;
        manualAreaCombo = new JComboBox<>(areaIds());
        litresField = new JTextField("2500", 12);
        priorityModeCombo = new JComboBox<>(new String[]{"Priority-based", "Manual / ID order"});
        JButton allocate = btn("Allocate water", true);
        c.gridx=0;c.gridy=0;form.add(new JLabel("Demand area"),c); c.gridx=1;form.add(manualAreaCombo,c);
        c.gridx=0;c.gridy=1;form.add(new JLabel("Requested litres"),c); c.gridx=1;form.add(litresField,c);
        c.gridx=0;c.gridy=2;form.add(new JLabel("Batch ordering mode"),c); c.gridx=1;form.add(priorityModeCombo,c);
        c.gridx=1;c.gridy=3;form.add(allocate,c);
        JButton batchPriority = btn("Allocate all sample demands (priority)", true);
        JButton batchManual = btn("Allocate all sample demands (ID order)", false);
        JPanel batchButtons = new JPanel(new FlowLayout(FlowLayout.LEFT)); batchButtons.setOpaque(false);
        batchButtons.add(batchPriority); batchButtons.add(batchManual);
        allocate.addActionListener(e -> {
            String id = (String)manualAreaCombo.getSelectedItem();
            Integer litres = parsePositive(litresField.getText(), "Enter a positive whole number of litres.");
            if (litres != null) { performAllocation(id, litres, false, "Manual request"); refreshAll(); }
        });
        batchPriority.addActionListener(e -> runBatch(true));
        batchManual.addActionListener(e -> runBatch(false));
        top.add(form, BorderLayout.CENTER); top.add(batchButtons, BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);

        JPanel lower = new JPanel(new GridLayout(1, 2, 12, 0)); lower.setOpaque(false);
        JPanel rq = card("Allocation requests", "Partial allocations keep unmet litres visible for later retries");
        requestTable = new JTable(requestModel); styleTable(requestTable); rq.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        JPanel areas = card("Demand status", "Reachability and outstanding demand update after faults and repairs");
        areaTable = new JTable(areaModel); styleTable(areaTable); areas.add(new JScrollPane(areaTable), BorderLayout.CENTER);
        lower.add(rq); lower.add(areas); p.add(lower, BorderLayout.CENTER);
        return p;
    }

    private JPanel networkPage() {
        JPanel p = page();
        JPanel top = card("Pipeline state and BFS reachability", "Only working pipeline links are considered by the graph traversal");
        JPanel controls = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 8)); controls.setOpaque(false);
        pipelineCombo = new JComboBox<>(pipelineIds());
        JButton fail = btn("Mark failed", true);
        JButton repair = btn("Repair", false);
        reservoirCombo = new JComboBox<>(reservoirIds());
        JComboBox<String> pathAreaCombo = new JComboBox<>(areaIds());
        JButton check = btn("Check path", false);
        fail.addActionListener(e -> failPipelineAction((String)pipelineCombo.getSelectedItem()));
        repair.addActionListener(e -> repairPipelineAction((String)pipelineCombo.getSelectedItem()));
        pathArea = new JTextArea(3, 55); pathArea.setEditable(false); pathArea.setLineWrap(true); pathArea.setWrapStyleWord(true);
        check.addActionListener(e -> {
            String rid = (String)reservoirCombo.getSelectedItem(), aid = (String)pathAreaCombo.getSelectedItem();
            List<String> path = network.getPath(rid, aid);
            pathArea.setText(path.isEmpty() ? rid + " cannot reach " + aid + " using working pipelines."
                    : "Reachable path: " + String.join(" → ", path) + "\nGraph reachability only; hydraulic pressure and flow are not simulated.");
        });
        controls.add(new JLabel("Pipeline")); controls.add(pipelineCombo); controls.add(fail); controls.add(repair);
        controls.add(new JLabel("Reservoir")); controls.add(reservoirCombo); controls.add(new JLabel("Area")); controls.add(pathAreaCombo); controls.add(check);
        top.add(controls, BorderLayout.NORTH);
        top.add(new JScrollPane(pathArea), BorderLayout.SOUTH);
        p.add(top, BorderLayout.NORTH);
        JPanel tbl = card("All pipeline links", "The project sample includes 12 links and 10 demand areas");
        pipelineTable = new JTable(pipelineModel); styleTable(pipelineTable); tbl.add(new JScrollPane(pipelineTable), BorderLayout.CENTER);
        p.add(tbl, BorderLayout.CENTER);
        return p;
    }

    private JPanel eventsPage() {
        JPanel p = page();
        JPanel upper = new JPanel(new GridLayout(1, 2, 12, 0)); upper.setOpaque(false);
        JPanel supply = card("Manual supply event", "Adjust a reservoir's available litres; values are clamped to 0..capacity");
        JPanel sf = new JPanel(new GridBagLayout()); sf.setOpaque(false);
        GridBagConstraints c = new GridBagConstraints(); c.insets = new Insets(5, 5, 5, 5); c.anchor = GridBagConstraints.WEST;
        supplyReservoirCombo = new JComboBox<>(reservoirIds()); supplyField = new JTextField("-1000", 10);
        JButton adjust = btn("Apply supply change", true); JButton dry = btn("Set selected reservoir to 0", false);
        c.gridx=0;c.gridy=0;sf.add(new JLabel("Reservoir"),c); c.gridx=1;sf.add(supplyReservoirCombo,c);
        c.gridx=0;c.gridy=1;sf.add(new JLabel("Change (+/- litres)"),c); c.gridx=1;sf.add(supplyField,c);
        c.gridx=1;c.gridy=2;sf.add(adjust,c); c.gridx=1;c.gridy=3;sf.add(dry,c);
        adjust.addActionListener(e -> {
            Integer delta = parseInteger(supplyField.getText(), "Enter a whole-number supply change.");
            if (delta != null) {
                Reservoir r = network.getReservoir((String)supplyReservoirCombo.getSelectedItem());
                r.setRemainingSupply(r.getRemainingSupply() + delta);
                logEvent("Supply update: " + r.getId() + " remaining supply is " + r.getRemainingSupply() + " L.");
                retryPendingRequests(); refreshAll(); message("Supply updated and pending requests retried.");
            }
        });
        dry.addActionListener(e -> {
            Reservoir r = network.getReservoir((String)supplyReservoirCombo.getSelectedItem());
            r.setRemainingSupply(0); logEvent("Dry reservoir test: " + r.getId() + " set to 0 L.");
            retryPendingRequests(); refreshAll(); message(r.getId() + " set to zero; pending requests retried.");
        });
        supply.add(sf, BorderLayout.CENTER); upper.add(supply);

        JPanel scenarios = card("Preset demo events (15 options)", "Choose a predefined demand, fault, repair, supply, or batch event");
        JPanel buttons = new JPanel(new BorderLayout(8, 8)); buttons.setOpaque(false);
        String[] presets = {
            "Critical demand: A3 + 4,500 L", "Critical demand: A7 + 3,000 L",
            "High-priority demand: A9 + 2,400 L", "Residential demand: A1 + 1,500 L",
            "Residential demand: A4 + 2,000 L", "Residential demand: A10 + 2,800 L",
            "Fail pipeline P9 (A9/A10 branch)", "Repair pipeline P9",
            "Fail pipeline P10 (A10 only)", "Repair pipeline P10",
            "Dry-reservoir test: R1 = 0 L", "Dry-reservoir test: R2 = 0 L",
            "Supply update: R1 + 1,000 L", "Reset daily supplies",
            "Run priority-based batch allocation"
        };
        presetEventCombo = new JComboBox<>(presets);
        JButton applyPreset = btn("Apply selected event", true);
        applyPreset.addActionListener(e -> runPresetEvent((String)presetEventCombo.getSelectedItem()));
        buttons.add(presetEventCombo, BorderLayout.CENTER); buttons.add(applyPreset, BorderLayout.SOUTH);
        scenarios.add(buttons, BorderLayout.CENTER); upper.add(scenarios);
        p.add(upper, BorderLayout.NORTH);

        JPanel eventlog = card("Event log", "Faults, repairs, supply updates and scenario actions");
        historyTable = new JTable(historyModel); styleTable(historyTable); eventlog.add(new JScrollPane(historyTable), BorderLayout.CENTER);
        p.add(eventlog, BorderLayout.CENTER);
        return p;
    }

    private JPanel historyPage() {
        JPanel p = page();
        JPanel upper = new JPanel(new GridLayout(1, 2, 12, 0)); upper.setOpaque(false);
        JPanel history = card("Allocation history", "Records allocation attempts, partial delivery and retry outcomes");
        requestTable = new JTable(requestModel); styleTable(requestTable); history.add(new JScrollPane(requestTable), BorderLayout.CENTER);
        upper.add(history);
        JPanel maintenance = card("Maintenance queue (FIFO)", "Failed pipelines create tickets; repairs resolve matching tickets");
        maintenanceTable = new JTable(maintenanceModel); styleTable(maintenanceTable); maintenance.add(new JScrollPane(maintenanceTable), BorderLayout.CENTER);
        upper.add(maintenance);
        p.add(upper, BorderLayout.CENTER);
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT)); actions.setOpaque(false);
        JButton retry = btn("Retry pending requests", true);
        JButton clear = btn("Clear resolved tickets from table", false);
        retry.addActionListener(e -> { retryPendingRequests(); refreshAll(); message("Pending requests retried against current connectivity and supply."); });
        clear.addActionListener(e -> { maintenanceQueue.removeIf(MaintenanceRequest::isResolved); maintenanceHistory.removeIf(MaintenanceRequest::isResolved); refreshAll(); });
        actions.add(retry); actions.add(clear); p.add(actions, BorderLayout.SOUTH);
        return p;
    }

    private void performAllocation(String areaId, int litres, boolean priorityMode, String label) {
        if (network.getArea(areaId) == null) { message("Unknown area: " + areaId); return; }
        WaterRequest req = new WaterRequest("WR-" + requestSequence.getAndIncrement(), areaId, litres,
                priorityMode ? "Priority" : "Manual");
        requests.add(req);
        int delivered = attemptRequest(req);
        logAllocation(req, label + " (delivered now " + delivered + " L)");
        refreshAll();
        if (delivered == 0 && req.getUnmetLitres() > 0) message("No water allocated: no reachable reservoir has remaining supply. Request kept pending.");
        else if (req.getUnmetLitres() > 0) message("Partial allocation: " + delivered + " L delivered; " + req.getUnmetLitres() + " L unmet.");
        else message("Allocation complete: " + delivered + " L delivered.");
    }

    private void createAndAllocate(String areaId, int litres, boolean priorityMode, String label) {
        performAllocation(areaId, litres, priorityMode, label);
    }

    private int attemptRequest(WaterRequest req) {
        if (req.isComplete()) return 0;
        DemandArea area = network.getArea(req.getAreaId());
        if (area == null) return 0;
        Reservoir r = network.chooseReservoirForArea(area.getId());
        if (r == null || r.getRemainingSupply() <= 0) return 0;
        int before = req.getUnmetLitres();
        int allocated = r.allocateWater(before);
        req.recordAllocation(allocated);
        if (allocated > 0) allocationHistory.add(now() + " | " + req.getId() + " → " + area.getName()
                + " via " + r.getName() + ": " + allocated + " L");
        return allocated;
    }

    private void runBatch(boolean priorityMode) {
        String mode = priorityMode ? "Priority-based" : "Manual / ID order";
        List<DemandArea> ordered = policy.orderAreas(network.getAreas(), priorityMode);
        int totalNow = 0;
        for (DemandArea a : ordered) {
            WaterRequest req = new WaterRequest("WR-" + requestSequence.getAndIncrement(), a.getId(), a.getDemand(), mode);
            requests.add(req);
            totalNow += attemptRequest(req);
        }
        logEvent(mode + " batch allocation executed; " + totalNow + " L delivered in this run.");
        retryPendingRequestsInOrder(priorityMode);
        refreshAll(); message(mode + " batch complete. Delivered this run: " + totalNow + " L.");
    }

    private void retryPendingRequests() { retryPendingRequestsInOrder(true); }

    private void retryPendingRequestsInOrder(boolean priorityMode) {
        List<WaterRequest> pending = new ArrayList<>();
        for (WaterRequest r : requests) if (!r.isComplete()) pending.add(r);
        if (priorityMode) {
            pending.sort((a,b) -> {
                int pa = network.getArea(a.getAreaId()).allocationPriority();
                int pb = network.getArea(b.getAreaId()).allocationPriority();
                int compare = Integer.compare(pb, pa);
                return compare != 0 ? compare : IdOrder.NATURAL.compare(a.getId(), b.getId());
            });
        } else {
            pending.sort(Comparator.comparing(WaterRequest::getId, IdOrder.NATURAL));
        }
        int total = 0;
        for (WaterRequest req : pending) total += attemptRequest(req);
        if (total > 0) logEvent("Pending requests retried: " + total + " L newly allocated.");
    }

    private void failSelectedOr(String id) { failPipelineAction(id); }
    private void repairSelectedOr(String id) { repairPipelineAction(id); }

    private void failPipelineAction(String id) {
        if (!network.failPipeline(id)) { message("Pipeline " + id + " is missing or already failed."); return; }
        MaintenanceRequest ticket = new MaintenanceRequest("MT-" + maintenanceSequence.getAndIncrement(), id, "Pipeline failure");
        maintenanceQueue.add(ticket); maintenanceHistory.add(ticket);
        logEvent("Pipeline " + id + " failed. Connectivity recalculated; pending requests retried.");
        retryPendingRequests(); refreshAll(); message("Pipeline " + id + " failed; maintenance ticket " + ticket.getId() + " created.");
    }

    private void repairPipelineAction(String id) {
        if (!network.repairPipeline(id)) { message("Pipeline " + id + " is missing or already working."); return; }
        for (MaintenanceRequest m : maintenanceQueue) {
            if (m.getPipelineId().equals(id) && !m.isResolved()) { m.markResolved(); break; }
        }
        logEvent("Pipeline " + id + " repaired. Connectivity recalculated; pending requests retried.");
        retryPendingRequests(); refreshAll(); message("Pipeline " + id + " repaired; pending requests retried.");
    }


    private void runPresetEvent(String event) {
        if (event == null) return;
        switch (event) {
            case "Critical demand: A3 + 4,500 L": createAndAllocate("A3",4500,true,"Preset critical-demand event"); break;
            case "Critical demand: A7 + 3,000 L": createAndAllocate("A7",3000,true,"Preset critical-demand event"); break;
            case "High-priority demand: A9 + 2,400 L": createAndAllocate("A9",2400,true,"Preset high-priority event"); break;
            case "Residential demand: A1 + 1,500 L": createAndAllocate("A1",1500,false,"Preset residential-demand event"); break;
            case "Residential demand: A4 + 2,000 L": createAndAllocate("A4",2000,false,"Preset residential-demand event"); break;
            case "Residential demand: A10 + 2,800 L": createAndAllocate("A10",2800,false,"Preset residential-demand event"); break;
            case "Fail pipeline P9 (A9/A10 branch)": failPipelineAction("P9"); break;
            case "Repair pipeline P9": repairPipelineAction("P9"); break;
            case "Fail pipeline P10 (A10 only)": failPipelineAction("P10"); break;
            case "Repair pipeline P10": repairPipelineAction("P10"); break;
            case "Dry-reservoir test: R1 = 0 L":
                network.getReservoir("R1").setRemainingSupply(0); logEvent("Dry-reservoir test: R1 set to 0 L.");
                retryPendingRequests(); refreshAll(); message("R1 set to zero; pending requests retried."); break;
            case "Dry-reservoir test: R2 = 0 L":
                network.getReservoir("R2").setRemainingSupply(0); logEvent("Dry-reservoir test: R2 set to 0 L.");
                retryPendingRequests(); refreshAll(); message("R2 set to zero; pending requests retried."); break;
            case "Supply update: R1 + 1,000 L":
                Reservoir r = network.getReservoir("R1"); r.setRemainingSupply(r.getRemainingSupply()+1000);
                logEvent("Preset supply update: R1 increased by up to 1,000 L."); retryPendingRequests(); refreshAll(); message("Supply updated; pending requests retried."); break;
            case "Reset daily supplies":
                for (Reservoir reservoir : network.getReservoirs()) reservoir.resetDailySupply();
                logEvent("New simulated day: reservoir supplies reset to configured capacity.");
                retryPendingRequests(); refreshAll(); message("Daily supplies reset. Historical allocations remain recorded."); break;
            case "Run priority-based batch allocation": runBatch(true); break;
            default: message("Unknown preset event.");
        }
    }

    private void refreshAll() {
        if (reservoirModel == null) return;
        reservoirModel.setRowCount(0);
        for (Reservoir r : network.getReservoirs())
            reservoirModel.addRow(new Object[]{r.getId(),r.getName(),r.getCapacity(),r.getRemainingSupply()});
        pipelineModel.setRowCount(0);
        for (Pipeline p : network.getPipelines())
            pipelineModel.addRow(new Object[]{p.getId(),p.getFrom(),p.getTo(),p.isWorking() ? "Working" : "Failed"});
        areaModel.setRowCount(0);
        for (DemandArea a : network.getAreas()) {
            int unmet = 0;
            for (WaterRequest r : requests) if (r.getAreaId().equals(a.getId())) unmet += r.getUnmetLitres();
            boolean reachable = false;
            for (Reservoir r : network.getReservoirs()) if (network.isReachable(r.getId(),a.getId())) { reachable=true; break; }
            areaModel.addRow(new Object[]{a.getId(),a.getName(),a.priorityLabel(),a.getDemand(),reachable ? "Yes" : "No",unmet});
        }
        requestModel.setRowCount(0);
        for (WaterRequest r : requests)
            requestModel.addRow(new Object[]{r.getId(),r.getAreaId(),r.getMode(),r.getRequestedLitres(),r.getAllocatedLitres(),r.getUnmetLitres(),r.getStatus()});
        maintenanceModel.setRowCount(0);
        for (MaintenanceRequest m : maintenanceHistory)
            maintenanceModel.addRow(new Object[]{m.getId(),m.getPipelineId(),m.getDescription(),m.getCreatedAt(),m.isResolved() ? "Resolved" : "Open"});
        historyModel.setRowCount(0);
        for (String h : eventHistory) historyModel.addRow(new Object[]{now(), h});
        for (String h : allocationHistory) historyModel.addRow(new Object[]{now(), h});
        persistence.saveSnapshot(network, requests, allocationHistory, eventHistory, maintenanceHistory);
        if (supplyMetric != null) {
            int remaining = 0, allocated = 0, unmet = 0, failed = 0;
            for (Reservoir r : network.getReservoirs()) remaining += r.getRemainingSupply();
            for (WaterRequest r : requests) { allocated += r.getAllocatedLitres(); unmet += r.getUnmetLitres(); }
            for (Pipeline p : network.getPipelines()) if (!p.isWorking()) failed++;
            setMetric(supplyMetric, remaining + " L", "SUPPLY REMAINING");
            setMetric(allocatedMetric, allocated + " L", "ALLOCATED THIS SESSION");
            setMetric(unmetMetric, unmet + " L", "UNMET REQUESTS");
            setMetric(failedMetric, String.valueOf(failed), "FAILED PIPELINES");
            Component[] components = getContentPane().getComponents();
            updateDashboardSummary(remaining, allocated, unmet, failed);
        }
        if (networkMapPanel != null) networkMapPanel.repaint();
        revalidate(); repaint();
    }

    private void updateDashboardSummary(int remaining, int allocated, int unmet, int failed) {
        findAndSetText(getContentPane(), "dashboardSummary",
                "Reservoirs: " + network.getReservoirs().size() + "\n"
                + "Demand areas: " + network.getAreas().size() + "\n"
                + "Pipelines: " + network.getPipelines().size() + "\n"
                + "Remaining supply: " + remaining + " L\n"
                + "Allocated total: " + allocated + " L\n"
                + "Unmet requests: " + unmet + " L\n"
                + "Failed pipelines: " + failed + "\n"
                + "Persistence: " + persistence.getStatus() + "\n\n"
                + "Allocation chooses a reachable reservoir with the greatest remaining supply. "
                + "A request is capped at the reservoir's remaining litres.");
    }

    private void findAndSetText(Container parent, String name, String value) {
        for (Component comp : parent.getComponents()) {
            if (comp instanceof JTextArea && name.equals(comp.getName())) { ((JTextArea)comp).setText(value); return; }
            if (comp instanceof Container) findAndSetText((Container)comp, name, value);
        }
    }

    private void setMetric(JLabel label, String value, String title) {
        label.setText("<html><div style='font-size:11px;color:#687a7c'>" + title
                + "</div><div style='font-size:23px;font-weight:bold;color:#223034'>" + value + "</div></html>");
    }

    private void styleTable(JTable table) {
        table.setRowHeight(27); table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        table.getTableHeader().setBackground(new Color(232, 240, 238));
        table.setGridColor(new Color(231, 237, 236)); table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
    }

    private void logEvent(String s) { eventHistory.add(now() + " | " + s); }
    private void logAllocation(WaterRequest r, String s) {
        logEvent(s + ": " + r.getId() + ", area " + r.getAreaId() + ", requested "
                + r.getRequestedLitres() + " L, total allocated " + r.getAllocatedLitres()
                + " L, unmet " + r.getUnmetLitres() + " L.");
    }
    private String now() { return java.time.LocalTime.now().withNano(0).toString(); }
    private int nextSequence(String[] ids, String prefix) {
        int max = 0;
        for (String id : ids) if (id != null && id.startsWith(prefix)) {
            try { max = Math.max(max, Integer.parseInt(id.substring(prefix.length()))); } catch (NumberFormatException ignored) {}
        }
        return max + 1;
    }
    private void message(String s) { if (messageLabel != null) messageLabel.setText(s); }
    private Integer parsePositive(String s, String error) {
        Integer n = parseInteger(s,error); if (n == null) return null;
        if (n <= 0) { message(error); return null; } return n;
    }
    private Integer parseInteger(String s, String error) {
        try { return Integer.parseInt(s.trim()); } catch (Exception ex) { message(error); return null; }
    }

    private String[] areaIds() { return network.getAreas().stream().map(DemandArea::getId).toArray(String[]::new); }
    private String[] pipelineIds() { return network.getPipelines().stream().map(Pipeline::getId).toArray(String[]::new); }
    private String[] reservoirIds() { return network.getReservoirs().stream().map(Reservoir::getId).toArray(String[]::new); }

    public static WaterNetwork createNetwork() {
        WaterNetwork n = new WaterNetwork();
        n.addReservoir(new Reservoir("R1","North Reservoir",10000));
        n.addReservoir(new Reservoir("R2","South Reservoir",8000));
        n.addArea(new ResidentialArea("A1","North Residential",2000,100));
        n.addArea(new ResidentialArea("A2","East Residential",1500,75));
        n.addArea(new CriticalInstitution("A3","Government Hospital",3000,"Hospital",100));
        n.addArea(new ResidentialArea("A4","Central Residential",2500,125));
        n.addArea(new ResidentialArea("A5","Government School Area",1800,90));
        n.addArea(new ResidentialArea("A6","South Residential",2200,110));
        n.addArea(new CriticalInstitution("A7","Emergency Centre",3500,"Emergency Centre",50));
        n.addArea(new ResidentialArea("A8","West Residential",1700,85));
        n.addArea(new HighPriorityArea("A9","Industrial Safety Zone",2400,"Industrial safety"));
        n.addArea(new ResidentialArea("A10","Agricultural Zone",2800,140));
        n.addPipeline(new Pipeline("P1","R1","A1"));
        n.addPipeline(new Pipeline("P2","R1","A2"));
        n.addPipeline(new Pipeline("P3","A1","A3"));
        n.addPipeline(new Pipeline("P4","A2","A4"));
        n.addPipeline(new Pipeline("P5","R2","A5"));
        n.addPipeline(new Pipeline("P6","R2","A6"));
        n.addPipeline(new Pipeline("P7","A5","A7"));
        n.addPipeline(new Pipeline("P8","A6","A8"));
        n.addPipeline(new Pipeline("P9","A3","A9"));
        n.addPipeline(new Pipeline("P10","A9","A10"));
        n.addPipeline(new Pipeline("P11","A1","A2"));
        n.addPipeline(new Pipeline("P12","A5","A6"));
        return n;
    }

    /** Draws the network topology from the same live Pipeline objects used by BFS. */
    private final class NetworkMapPanel extends JPanel {
        private final Map<String, Point> layout = new LinkedHashMap<>();
        private final Map<String, String> labels = new LinkedHashMap<>();
        NetworkMapPanel() {
            setOpaque(true); setBackground(Color.WHITE);
            setPreferredSize(new Dimension(900, 510));
            setMinimumSize(new Dimension(650, 420));
            layout.put("R1", new Point(75, 115));
            layout.put("A1", new Point(220, 85));
            layout.put("A2", new Point(220, 205));
            layout.put("A3", new Point(380, 45));
            layout.put("A4", new Point(380, 230));
            layout.put("A9", new Point(545, 65));
            layout.put("A10", new Point(710, 65));
            layout.put("R2", new Point(75, 370));
            layout.put("A5", new Point(220, 345));
            layout.put("A6", new Point(220, 455));
            layout.put("A7", new Point(380, 325));
            layout.put("A8", new Point(380, 455));
            for (DemandArea a : network.getAreas()) labels.put(a.getId(), a.getName());
            for (Reservoir r : network.getReservoirs()) labels.put(r.getId(), r.getName());
        }
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double sx = Math.max(0.65, (getWidth() - 80.0) / 800.0);
            double sy = Math.max(0.65, (getHeight() - 55.0) / 480.0);
            Map<String, Point> pts = new HashMap<>();
            for (Map.Entry<String, Point> e : layout.entrySet())
                pts.put(e.getKey(), new Point(35 + (int)(e.getValue().x * sx), 18 + (int)(e.getValue().y * sy)));
            for (Pipeline pipe : network.getPipelines()) {
                Point a = pts.get(pipe.getFrom()), b = pts.get(pipe.getTo());
                if (a == null || b == null) continue;
                boolean working = pipe.isWorking();
                g.setColor(working ? new Color(44, 145, 123) : new Color(196, 43, 51));
                g.setStroke(working ? new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)
                        : new BasicStroke(3.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND, 10f, new float[]{8f, 5f}, 0f));
                int x1=a.x, y1=a.y, x2=b.x, y2=b.y;
                g.drawLine(x1, y1, x2, y2);
                int mx=(x1+x2)/2, my=(y1+y2)/2;
                g.setFont(new Font("SansSerif", Font.BOLD, 11));
                String pid=pipe.getId();
                g.setColor(working ? new Color(35, 105, 93) : new Color(180, 30, 40));
                g.drawString(pid, mx+4, my-4);
                if (!working) {
                    g.setColor(new Color(196, 43, 51));
                    g.setStroke(new BasicStroke(4f));
                    g.drawLine(mx-7,my-7,mx+7,my+7); g.drawLine(mx-7,my+7,mx+7,my-7);
                    g.setFont(new Font("SansSerif", Font.BOLD, 10));
                    g.drawString("BROKEN", mx+9, my+14);
                }
            }
            for (Map.Entry<String, Point> e : pts.entrySet()) {
                String id=e.getKey(); Point pt=e.getValue();
                int w=id.startsWith("R") ? 96 : 100, h=42;
                Color fill;
                if (id.startsWith("R")) fill = new Color(35,119,111);
                else {
                    DemandArea area=network.getArea(id);
                    if (area instanceof CriticalInstitution) fill = new Color(255, 231, 177);
                    else if (area instanceof HighPriorityArea) fill = new Color(224, 210, 248);
                    else fill = new Color(225, 239, 236);
                }
                int x=pt.x-w/2, y=pt.y-h/2;
                g.setColor(new Color(30, 45, 48, 25)); g.fillRoundRect(x+2,y+3,w,h,14,14);
                g.setColor(fill); g.fillRoundRect(x,y,w,h,14,14);
                g.setColor(id.startsWith("R") ? new Color(23,83,78) : new Color(155,174,169));
                g.setStroke(new BasicStroke(1.2f)); g.drawRoundRect(x,y,w,h,14,14);
                String main = id + (id.startsWith("R") ? " · RESERVOIR" : "");
                g.setFont(new Font("SansSerif", Font.BOLD, 12));
                g.setColor(id.startsWith("R") ? Color.WHITE : new Color(34,48,52));
                FontMetrics fm=g.getFontMetrics();
                g.drawString(main, pt.x-fm.stringWidth(main)/2, pt.y-2);
                String name=labels.getOrDefault(id, id);
                if (name.length()>15) name=name.substring(0,14)+"…";
                g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                g.setColor(id.startsWith("R") ? new Color(232,250,247) : new Color(76,94,94));
                fm=g.getFontMetrics(); g.drawString(name,pt.x-fm.stringWidth(name)/2,pt.y+13);
            }
            g.dispose();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new SwingMain().setVisible(true));
    }
}
