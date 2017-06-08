package org.openecomp.config.gui.app;


import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;
import org.openecomp.config.api.ConfigurationManager;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Checkbox;
import java.awt.CheckboxGroup;
import java.awt.Choice;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.ScrollPane;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.event.ItemEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.management.JMX;
import javax.management.MBeanServerConnection;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;


/**
 * The type Configuration.
 */
public class Configuration extends Frame {

  /**
   * The Vm.
   */
  VirtualMachine vm = null;
  private TextField host;
  private TextField port;
  private TextField user;
  private Choice pid;
  private TextField password;
  private CheckboxGroup getUpdateList;
  private CheckboxGroup localRemote;
  private Choice tenantChoice;
  private Choice namespaceChoice;
  private Choice keyChoice;
  private Checkbox latest;
  private Checkbox lookup;
  private Checkbox fallback;
  private Checkbox nodeSpecific;
  private Checkbox nodeSpecificForList;
  private Checkbox latestForList;
  private Checkbox lookupForList;
  private Checkbox fallbackForList;
  private Checkbox nodeOverride;
  private TextArea getValue = new TextArea(1, 50);
  private TextArea updateValue = new TextArea(1, 50);
  private Panel table;
  private CardLayout cards;
  private Panel cardPanel;
  private JMXConnector connector;
  private ConfigurationManager manager;
  private Frame container;
  private Button getKeysButton;

  /**
   * Instantiates a new Configuration.
   *
   * @param title the title
   */
  public Configuration(String title) {
    super(title);
    setSize(300, 300);
    setLayout(new GridLayout(2, 1));
    Panel firstRowPanel = new Panel();
    firstRowPanel.setLayout(new GridLayout(0, 1));
    firstRowPanel.add(
        buildHostPortPanel(host = new TextField("127.0.0.1"), port = new TextField("9999"),
            pid = new Choice(), localRemote = new CheckboxGroup()));
    firstRowPanel
        .add(buildUserPasswordPanel(user = new TextField(""), password = new TextField("")));
    firstRowPanel.add(buildGetUpdatelistPanel(getUpdateList = new CheckboxGroup()));
    firstRowPanel.add(
        buildTenantNamespacePanel(tenantChoice = new Choice(), namespaceChoice = new Choice()));
    firstRowPanel.add(buildKeyPanel(keyChoice = new Choice()));
    add(firstRowPanel);
    add(cardPanel = buildCards(cards = new CardLayout()));
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent windowEvent) {
        close();
        System.exit(0);
      }
    });
    pack();
    setVisible(true);
    container = this;
    centreWindow(this);
    Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
      this.populateAvailablePids();
      validate();
    }, 30, 30, TimeUnit.SECONDS);
  }

  /**
   * The entry point of application.
   *
   * @param args the input arguments
   */
  public static void main(String[] args) {
    Configuration configuration = new Configuration("Configuration Management App");

  }

  private void toggleLocalRemote(ItemEvent ie) {
    if (ie.getStateChange() == ItemEvent.SELECTED) {
      String usecase = (ie.getSource() instanceof Checkbox) ? ((Checkbox) ie.getSource()).getLabel()
          : ((Checkbox) ie.getItem()).getLabel();
      pid.setEnabled(usecase.equals("Local"));
      host.setEnabled(!usecase.equals("Local"));
      port.setEnabled(!usecase.equals("Local"));
    }
  }

  private Panel buildHostPortPanel(TextField host, TextField port, Choice pid,
                                   CheckboxGroup group) {
    populateAvailablePids();
    Panel panel = new Panel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = 0;
    Checkbox checkbox = null;
    panel.add(checkbox = new Checkbox("Local", group, pid.getItemCount() > 0), gbc);
    checkbox.addItemListener(this::toggleLocalRemote);
    gbc.weightx = 0;
    panel.add(checkbox = new Checkbox("Remote", group, pid.getItemCount() == 0), gbc);
    checkbox.addItemListener(this::toggleLocalRemote);
    gbc.weightx = 0;
    panel.add(new Label("PID:"), gbc);
    gbc.weightx = 1;
    panel.add(pid, gbc);
    gbc.weightx = 0;
    panel.add(new Label("Host:"), gbc);
    gbc.weightx = 1;
    panel.add(host, gbc);
    gbc.weightx = 0;
    panel.add(new Label("Port:"), gbc);
    gbc.weightx = 1;
    panel.add(port, gbc);
    host.setEnabled(pid.getItemCount() == 0);
    port.setEnabled(pid.getItemCount() == 0);
    pid.setEnabled(pid.getItemCount() > 0);
    return panel;
  }

  private Panel buildUserPasswordPanel(TextField user, TextField password) {
    Panel panel = new Panel();
    password.setEchoChar('*');
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = 0;
    panel.add(new Label("User : "), gbc);
    gbc.weightx = 1;
    panel.add(user, gbc);
    gbc.weightx = 0;
    panel.add(new Label("Password : "), gbc);
    gbc.weightx = 1;
    panel.add(password, gbc);
    gbc.weightx = 0;
    Button button = null;
    panel.add(button = new Button("Connect"), gbc);
    button.addActionListener((actionListener) -> connect());
    return panel;
  }

  /**
   * Build get updatelist panel panel.
   *
   * @param getUpdtaeList the get updtae list
   * @return the panel
   */
  public Panel buildGetUpdatelistPanel(CheckboxGroup getUpdtaeList) {
    Panel panel = new Panel();
    Checkbox checkbox = null;
    panel.setLayout(new GridLayout(1, 3));
    panel.add(checkbox = new Checkbox("Get", getUpdtaeList, true));
    checkbox.addItemListener(this::showCard);
    panel.add(checkbox = new Checkbox("List", getUpdtaeList, false));
    checkbox.addItemListener(this::showCard);
    panel.add(checkbox = new Checkbox("Update", getUpdtaeList, false));
    checkbox.addItemListener(this::showCard);
    return panel;
  }

  /**
   * Build tenant namespace panel panel.
   *
   * @param tenant    the tenant
   * @param namespace the namespace
   * @return the panel
   */
  public Panel buildTenantNamespacePanel(Choice tenant, Choice namespace) {
    Panel panel = new Panel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = 0;
    panel.add(new Label("Tenant"), gbc);
    gbc.weightx = 1;
    panel.add(tenant, gbc);
    gbc.weightx = 0;
    panel.add(new Label("Namespace"), gbc);
    gbc.weightx = 1;
    panel.add(namespace, gbc);
    gbc.weightx = 0;
    panel.add(getKeysButton = new Button("Get Keys"), gbc);
    getKeysButton.addActionListener((actionListener) -> populateKeys(
        manager.getKeys(tenantChoice.getSelectedItem(), namespaceChoice.getSelectedItem())));
    tenantChoice.addItemListener((itemListener) -> keyChoice.removeAll());
    namespaceChoice.addItemListener((itemListener) -> keyChoice.removeAll());
    return panel;
  }

  private Panel buildConnectPanel() {
    Button button = null;
    Panel panel = new Panel();
    panel.setLayout(new GridLayout(1, 3));
    panel.add(new Label());
    panel.add(button = new Button("Connect"));
    panel.add(new Label());
    button.addActionListener((actionListener) -> connect());
    return panel;
  }

  /**
   * Build key panel panel.
   *
   * @param keyChoice the key choice
   * @return the panel
   */
  public Panel buildKeyPanel(Choice keyChoice) {
    Panel panel = new Panel();
    panel.setLayout(new GridBagLayout());
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.fill = gbc.HORIZONTAL;
    gbc.weightx = 0;
    panel.add(new Label("Keys : "), gbc);
    gbc.weightx = 1;
    panel.add(keyChoice, gbc);
    return panel;
  }

  /**
   * Build cards panel.
   *
   * @param cards the cards
   * @return the panel
   */
  public Panel buildCards(CardLayout cards) {
    Panel panel = new Panel();
    panel.setLayout(cards);
    panel.add(buildCardGetPanel(latest = new Checkbox("Latest"),
        fallback = new Checkbox("Fallback"),
        lookup = new Checkbox("External Lookup"), nodeSpecific = new Checkbox("Node Specific"),
        getValue), "Get");
    panel.add(buildCardUpdatePanel(nodeOverride = new Checkbox("NodeOverride"),
        updateValue), "Update");
    panel.add(buildCardListPanel(table = new Panel(), latestForList = new Checkbox("Latest"),
        fallbackForList = new Checkbox("Fallback"), lookupForList = new Checkbox("External Lookup"),
        nodeSpecificForList = new Checkbox("Node Specific")), "List");
    return panel;
  }

  /**
   * Build card get panel panel.
   *
   * @param latest       the latest
   * @param fallback     the fallback
   * @param lookup       the lookup
   * @param nodeSpecific the node specific
   * @param value        the value
   * @return the panel
   */
  public Panel buildCardGetPanel(Checkbox latest, Checkbox fallback, Checkbox lookup,
                                 Checkbox nodeSpecific, TextArea value) {
    Panel panel = new Panel();

    panel.setLayout(new BorderLayout());
    Panel p1 = new Panel();
    p1.setLayout(new GridLayout(1, 4));
    p1.add(latest);
    p1.add(fallback);
    p1.add(nodeSpecific);
    p1.add(lookup);
    panel.add(p1, BorderLayout.NORTH);
    panel.add(value, BorderLayout.CENTER);
    Panel bottom = new Panel();
    bottom.setLayout(new GridLayout(1, 5));
    bottom.add(new Label());
    Button button = null;
    bottom.add(button = new Button("GET"));
    button.addActionListener((actionListener) -> value.setText(manager
        .getConfigurationValue(buildMap("org.openecomp.config.type.ConfigurationQuery", value))));
    bottom.add(new Label());
    bottom.add(button = new Button("CLOSE"));
    bottom.add(new Label());
    panel.add(bottom, BorderLayout.SOUTH);
    button.addActionListener((actionListener) -> {
      close();
      System.exit(0);
    });
    return panel;
  }

  private Map<String, Object> buildMap(String implClass, TextArea value) {
    Map<String, Object> input = new HashMap<>();
    input.put("ImplClass", implClass);//:);
    input.put("externalLookup", value != null ? lookup.getState() : lookupForList.getState());
    input.put("nodeOverride", nodeOverride.getState());
    input.put("latest", value != null ? latest.getState() : latestForList.getState());
    input.put("fallback", value != null ? fallback.getState() : fallbackForList.getState());
    input.put("nodeSpecific",
        value != null ? nodeSpecific.getState() : nodeSpecificForList.getState());
    input.put("tenant", tenantChoice.getSelectedItem());
    input.put("namespace", namespaceChoice.getSelectedItem());
    if (keyChoice.getItemCount() == 0) {
      message("Error", "Key is missing.");
      throw new RuntimeException("Error");
    }
    input.put("key", keyChoice.getSelectedItem());
    input.put("value", value == null ? "" : value.getText());
    return input;
  }

  /**
   * Build card update panel panel.
   *
   * @param nodeOverride the node override
   * @param value        the value
   * @return the panel
   */
  public Panel buildCardUpdatePanel(Checkbox nodeOverride, TextArea value) {
    Panel panel = new Panel();
    panel.setLayout(new BorderLayout());
    Panel p1 = new Panel();
    p1.setLayout(new GridLayout(1, 1));
    p1.add(nodeOverride);
    panel.add(p1, BorderLayout.NORTH);
    panel.add(value, BorderLayout.CENTER);
    Panel bottom = new Panel();
    bottom.setLayout(new GridLayout(1, 5));
    bottom.add(new Label());
    Button button = null;
    bottom.add(button = new Button("UPDATE"));
    button.addActionListener((actionListener) -> {
      manager
          .updateConfigurationValue(buildMap("org.openecomp.config.type.ConfigurationUpdate", value));
      message("Info", "Success");
    });
    bottom.add(new Label());
    bottom.add(button = new Button("CLOSE"));
    bottom.add(new Label());
    panel.add(bottom, BorderLayout.SOUTH);
    button.addActionListener((actionListener) -> {
      close();
      System.exit(0);
    });
    return panel;
  }

  /**
   * Build card list panel panel.
   *
   * @param table        the table
   * @param latest       the latest
   * @param fallback     the fallback
   * @param nodeSpecific the node specific
   * @param lookup       the lookup
   * @return the panel
   */
  public Panel buildCardListPanel(Panel table, Checkbox latest, Checkbox fallback,
                                  Checkbox nodeSpecific, Checkbox lookup) {
    Panel panel = new Panel();
    panel.setLayout(new BorderLayout());
    table.setLayout(new GridLayout(0, 2));
    ScrollPane sp = new ScrollPane();
    sp.add(table);
    panel.add(sp, BorderLayout.CENTER);
    Panel p1 = new Panel();
    p1.setLayout(new GridLayout(1, 4));
    p1.add(latest);
    p1.add(fallback);
    p1.add(lookup);
    p1.add(nodeSpecific);
    panel.add(p1, BorderLayout.NORTH);
    Panel bottom = new Panel();
    bottom.setLayout(new GridLayout(1, 5));
    bottom.add(new Label());
    Button button = null;
    bottom.add(button = new Button("List"));
    button.addActionListener((actionListener) -> populateKeyValue(
        manager.listConfiguration(buildMap("org.openecomp.config.type.ConfigurationQuery", null))));
    bottom.add(new Label());
    bottom.add(button = new Button("CLOSE"));
    bottom.add(new Label());
    panel.add(bottom, BorderLayout.SOUTH);
    button.addActionListener((actionListener) -> {
      close();
      System.exit(0);
    });
    return panel;
  }

  private void populateKeyValue(Map<String, String> collection) {
    table.removeAll();
    Set<String> keys = collection.keySet();
    for (String key : keys) {
      table.add(new Label(key));
      table.add(new Label(collection.get(key)));
    }
    table.validate();
  }

  private void showCard(ItemEvent ie) {
    if (ie.getStateChange() == ItemEvent.SELECTED) {
      String usecase = (ie.getSource() instanceof Checkbox) ? ((Checkbox) ie.getSource()).getLabel()
          : ((Checkbox) ie.getItem()).getLabel();
      cards.show(cardPanel, usecase);
      if (usecase.equals("List")) {
        keyChoice.removeAll();
      }
      keyChoice.setEnabled(!usecase.equals("List"));
      getKeysButton.setEnabled(!usecase.equals("List"));
    }
  }

  private void close() {
    try {
      if (connector != null) {
        connector.close();
        connector = null;
        vm.detach();
      }
    } catch (Exception exception) {
      //Do nothing
    }
  }

  private void connect() {
    try {
      close();
      if (!validateHostPort()) {
        return;
      }
      JMXServiceURL url = null;
      if (localRemote.getSelectedCheckbox().getLabel().equals("Local")) {
        url = new JMXServiceURL(
            (vm = VirtualMachine.attach(pid.getSelectedItem())).getAgentProperties()
                .getProperty("com.sun.management.jmxremote.localConnectorAddress"));
      } else {
        url = new JMXServiceURL(
            "service:jmx:rmi:///jndi/rmi://" + host.getText() + ":" + port.getText() + "/jmxrmi");
      }
      Map<String, String[]> env = new HashMap<>();
      String[] credentials = {user.getText(), password.getText()};
      env.put(JMXConnector.CREDENTIALS, credentials);
      connector = JMXConnectorFactory.connect(url, env);
      MBeanServerConnection mbsc = connector.getMBeanServerConnection();
      ObjectName mbeanName = new ObjectName("org.openecomp.jmx:name=SystemConfig");
      manager = JMX.newMBeanProxy(mbsc, mbeanName, ConfigurationManager.class, true);
      message("Message", "Success!!!");
      populateTenants(manager.getTenants());
      populateNamespaces(manager.getNamespaces());
    } catch (Exception exception) {
      message("Error", exception.getMessage());
    }
  }

  private boolean validateHostPort() {
    if (localRemote.getSelectedCheckbox().getLabel().equals("Local")) {
      if (pid.getSelectedItem() == null || pid.getSelectedItem().trim().length() == 0
          || !pid.getSelectedItem().matches("^[1-9][0-9]*$")) {
        message("Error", "pid is mandatory numeric value greater than 0.");
        return false;
      }
    } else {
      if (host.getText() == null || host.getText().trim().length() == 0) {
        message("Error", "Host is mandatory.");
        return false;
      }
      if (port.getText() == null || port.getText().trim().length() == 0
          || !port.getText().matches("^[1-9][0-9]*$")) {
        message("Error", "Port is mandatory numeric value greater than 0.");
        return false;
      }
    }
    return true;
  }

  private void populateTenants(Collection<String> collection) {
    tenantChoice.removeAll();
    for (String item : collection) {
      tenantChoice.add(item);
    }
  }

  private void populateNamespaces(Collection<String> collection) {
    namespaceChoice.removeAll();
    keyChoice.removeAll();
    for (String item : collection) {
      namespaceChoice.add(item);
    }
  }

  private void populateKeys(Collection<String> collection) {
    keyChoice.removeAll();
    for (String item : collection) {
      keyChoice.add(item);
    }
  }

  private void message(String title, String text) {
    Dialog dialog = new Dialog(container, title, true);
    dialog.setLayout(new BorderLayout());
    dialog.add(new Label(text), BorderLayout.CENTER);
    Button ok = new Button("OK");
    Panel panel = new Panel();
    panel.setLayout(new GridLayout(1, 3));
    panel.add(new Label());
    panel.add(ok);
    panel.add(new Label());
    ok.addActionListener((actionListener) -> dialog.setVisible(false));
    dialog.add(panel, BorderLayout.SOUTH);
    dialog.pack();
    centerDialog(dialog);
    dialog.setVisible(true);
  }

  private void centreWindow(Frame frame) {
    Dimension dimension = Toolkit.getDefaultToolkit().getScreenSize();
    int x1 = (int) ((dimension.getWidth() - frame.getWidth()) / 2);
    int y1 = (int) ((dimension.getHeight() - frame.getHeight()) / 2);
    frame.setLocation(x1, y1);
  }

  private void centerDialog(Dialog dialog) {
    Dimension dimension = container.getSize();
    container.getLocationOnScreen();
    int x1 =
        container.getLocationOnScreen().x + (int) ((dimension.getWidth() - dialog.getWidth()) / 2);
    int y1 = container.getLocationOnScreen().y
        + (int) ((dimension.getHeight() - dialog.getHeight()) / 2);
    dialog.setLocation(x1, y1);
  }

  private void setWindowPosition(Frame window, int screen) {
    GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
    GraphicsDevice[] allDevices = env.getScreenDevices();
    int topLeftX;
    int topLeftY;
    int screenX;
    int screenY;
    int windowPosX;
    int windowPosY;

    if (screen < allDevices.length && screen > -1) {
      topLeftX = allDevices[screen].getDefaultConfiguration().getBounds().x;
      topLeftY = allDevices[screen].getDefaultConfiguration().getBounds().y;

      screenX = allDevices[screen].getDefaultConfiguration().getBounds().width;
      screenY = allDevices[screen].getDefaultConfiguration().getBounds().height;
    } else {
      topLeftX = allDevices[0].getDefaultConfiguration().getBounds().x;
      topLeftY = allDevices[0].getDefaultConfiguration().getBounds().y;

      screenX = allDevices[0].getDefaultConfiguration().getBounds().width;
      screenY = allDevices[0].getDefaultConfiguration().getBounds().height;
    }

    windowPosX = ((screenX - window.getWidth()) / 2) + topLeftX;
    windowPosY = ((screenY - window.getHeight()) / 2) + topLeftY;

    window.setLocation(windowPosX, windowPosY);
  }

  private void populateAvailablePids() {
    pid.removeAll();
    for (VirtualMachineDescriptor desc : VirtualMachine.list()) {
      try {
        VirtualMachine vm = null;
        JMXServiceURL url = new JMXServiceURL(
            (vm = VirtualMachine.attach(desc.id())).getAgentProperties()
                .getProperty("com.sun.management.jmxremote.localConnectorAddress"));
        Set set = JMXConnectorFactory.connect(url, null).getMBeanServerConnection()
            .queryMBeans(new ObjectName("org.openecomp.jmx:name=SystemConfig"), null);
        if (!set.isEmpty()) {
          pid.add(desc.id());
        }
        vm.detach();
      } catch (Exception exception) {
        //do nothing
      }
    }
  }
}
