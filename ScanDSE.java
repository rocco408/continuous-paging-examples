import com.datastax.driver.core.*;
import com.datastax.driver.dse.DseSession;
import com.datastax.driver.dse.DseCluster;
import com.datastax.driver.core.ContinuousPagingSession;
import com.datastax.driver.core.ContinuousPagingOptions;
import static com.datastax.driver.core.ContinuousPagingOptions.PageUnit.BYTES;
import static com.datastax.driver.core.ContinuousPagingOptions.PageUnit.ROWS;

public class ScanDSE {

    public static void main(String[] args) {

        /////////////////////////
        // Continuous Paging
        DseCluster cluster = DseCluster.builder()
            .addContactPoint("127.0.0.1")
            .withSocketOptions(new SocketOptions().setReadTimeoutMillis(720000))
            .build();

        // Assumes we've already inserted data into ks.tab using the writeperfrow test from SCS.
        ContinuousPagingSession cpSession = (ContinuousPagingSession) cluster.connect("ks"); 
        ContinuousPagingOptions cpOptions = ContinuousPagingOptions.builder().withPageSize(100, ROWS).build();
        SimpleStatement statement = new SimpleStatement(String.format("SELECT COUNT(*) FROM ks.tab"));

        long wallClockStartTime = System.nanoTime();
        ContinuousPagingResult cpResult = cpSession.executeContinuously(statement, cpOptions);

        for (Row row : cpResult) {
            System.out.format("Scan with Continuous Paging, Number of rows counted: %d\n", row.getLong("count"));
        }

        long wallClockStopTime = System.nanoTime();
        long wallClockTimeDiff = wallClockStopTime - wallClockStartTime;
        long wallClockTimeSeconds = (long) (wallClockTimeDiff / 1000000000.0);
        System.out.format("Scan with ContinuousPaging, TimeInSeconds: %d\n\n", wallClockTimeSeconds);
        
        /////////////////////////
        // No Continuous Paging 
        Session session = cluster.connect("ks");
                
        wallClockStartTime = System.nanoTime();
        ResultSet result = session.execute(statement);

        for (Row row : result) {
            System.out.format("Scan without Continuous Paging, Number of rows counted: %d\n", row.getLong("count"));
        }

        wallClockStopTime = System.nanoTime();
        wallClockTimeDiff = wallClockStopTime - wallClockStartTime;
        wallClockTimeSeconds = (long) (wallClockTimeDiff / 1000000000.0);
        System.out.format("Scan without Continuous Paging, TimeInSeconds: %d\n\n", wallClockTimeSeconds);

        cluster.close();
    }

}
