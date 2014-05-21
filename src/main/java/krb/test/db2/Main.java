package krb.test.db2;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

/**
 * @author Martin Simka
 */
public class Main {
    //private static final String JDBC_URL = "jdbc:db2://vmg06.mw.lab.eng.bos.redhat.com:50001/QAKRB";
    private static final String JDBC_URL = "jdbc:db2://db16.mw.lab.eng.bos.redhat.com:50001/qakrb";


    public static void main(String[] args) throws Exception {
//        System.setProperty("java.security.auth.login.config", "/home/msimka/Projekty/redhat/tmp/db2-jdbc-client/login.conf");
        System.setProperty("sun.security.krb5.debug", "true");
        System.setProperty("java.security.krb5.kdc", "kerberos-test.mw.lab.eng.bos.redhat.com");
        System.setProperty("java.security.krb5.realm", "MW.LAB.ENG.BOS.REDHAT.COM");
//        System.setProperty("java.security.krb5.conf", "/home/msimka/Projekty/redhat/git/jbossqe-eap-tests-kerberos-datasource/krb5.conf");

        Properties props = new Properties();
        //props.put("kerberosServerPrincipal", "kerberos-test.mw.lab.eng.bos.redhat.com");
        props.put("securityMechanism",
                new String("" + com.ibm.db2.jcc.DB2BaseDataSource.KERBEROS_SECURITY + ""));

        //DriverManager.registerDriver(new DB2Driver());
        Connection conn = DriverManager.getConnection(JDBC_URL, props);

        printUserName(conn);

        conn.close();
    }

    private static void printUserName(Connection conn) throws SQLException {
        Statement stmt = null;
        try {
            stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT CURRENT USER FROM SYSIBM.SYSDUMMY1");
            while (rs.next())
                System.out.println("User is: " + rs.getString(1));
            rs.close();
        } finally {
            if (stmt != null)
                stmt.close();
        }
    }
}
