package krb.test.db2;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import java.io.File;
import java.security.PrivilegedExceptionAction;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author Martin Simka
 */
public class Main {
    private static final Logger logger = Logger.getLogger(Main.class.getName());
    //private static final String JDBC_URL = "jdbc:db2://vmg06.mw.lab.eng.bos.redhat.com:50001/QAKRB";
    private static final String JDBC_URL = "jdbc:db2://db16.mw.lab.eng.bos.redhat.com:50001/qakrb";


    public static void main(String[] args) throws Exception {
        FileHandler handler = new FileHandler("log.txt", false);
        handler.setFormatter(new SimpleFormatter());
        handler.setLevel(Level.ALL);
        logger.addHandler(handler);

        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("java.security.krb5.realm", "MW.LAB.ENG.BOS.REDHAT.COM");
        System.setProperty("java.security.krb5.kdc", "kerberos-test.mw.lab.eng.bos.redhat.com");
        System.setProperty("java.security.krb5.conf", new File("krb5.conf").getAbsolutePath());

        class Krb5LoginConfiguration extends Configuration {

            private final AppConfigurationEntry[] configList = new AppConfigurationEntry[1];

            public Krb5LoginConfiguration() {
                Map<String, String> options = new HashMap<String, String>();
                options.put("storeKey", "false");
                options.put("useKeyTab", "true");
                options.put("keyTab", "KRBUSR01");
                options.put("principal", "KRBUSR01@MW.LAB.ENG.BOS.REDHAT.COM");
                options.put("doNotPrompt", "true");
                options.put("useTicketCache", "true");
                options.put("ticketCache", "/tmp/krbcc_1000");
                options.put("refreshKrb5Config", "true");
                options.put("isInitiator", "true");
                options.put("addGSSCredential", "true");
                configList[0] = new AppConfigurationEntry(
                        "org.jboss.security.negotiation.KerberosLoginModule",
                        AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
                        options);
            }

            @Override
            public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                return configList;
            }
        }

        Configuration.setConfiguration(new Krb5LoginConfiguration());
        final LoginContext lc = new LoginContext("test");
        lc.login();
        Subject subject = lc.getSubject();

        final Properties props = new Properties();
        props.put("securityMechanism",
                new String("" + com.ibm.db2.jcc.DB2BaseDataSource.KERBEROS_SECURITY + ""));
        props.put("user", "KRBUSR01@MW.LAB.ENG.BOS.REDHAT.COM");
        props.put("password", "");

        Connection conn = null;
        try {
            conn = Subject.doAs(subject, new PrivilegedExceptionAction<Connection>() {
                @Override
                public Connection run() throws Exception {
                    return DriverManager.getConnection(JDBC_URL, props);
                }
            });

            printUserName(conn);
            conn.close();
        } catch (Throwable t) {
            logger.log(Level.SEVERE, "", t);
        } finally {
            if(conn != null)
                conn.close();
            if(lc != null)
                lc.logout();
        }
    }

    private static void printUserName(Connection conn) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("SELECT CURRENT USER FROM SYSIBM.SYSDUMMY1");
            while (rs.next())
                System.out.println("User is: " + rs.getString(1));
            rs.close();
        } finally {
            if(rs != null)
                rs.close();
            if (stmt != null)
                stmt.close();
        }
    }
}
