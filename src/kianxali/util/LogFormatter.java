package kianxali.util;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

// based on http://stackoverflow.com/questions/2950704/java-util-logging-how-to-suppress-date-line
public class LogFormatter extends Formatter {
    @Override
    public String format(final LogRecord r) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%7s: ", r.getLevel()));
        sb.append(formatMessage(r)).append(System.getProperty("line.separator"));
        if (null != r.getThrown()) {
            sb.append("Throwable occurred: ");
            Throwable t = r.getThrown();
            PrintWriter pw = null;
            try {
                StringWriter sw = new StringWriter();
                pw = new PrintWriter(sw);
                t.printStackTrace(pw);
                sb.append(sw.toString());
            } finally {
                if (pw != null) {
                    try {
                        pw.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return sb.toString();
    }
}
