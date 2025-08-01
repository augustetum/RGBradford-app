import com.rgbradford.backend.dto.request.PlateAnalysisParams;
import com.rgbradford.backend.dto.response.WellAnalysisResult;
import com.rgbradford.backend.service.impl.PlateAnalysisServiceImpl;
import java.util.List;

public class PlateAnalysisServiceTest {
    public static void main(String[] args) throws Exception {
        // Path to your image in the repository
        String imagePath = "fiji/plate.jpg"; // Adjust if needed

        // Set up parameters (adjust as needed for your image)
        PlateAnalysisParams params = new PlateAnalysisParams(
            12, // columns
            8,  // rows
            294, // xOrigin
            220, // yOrigin
            1434, // xEnd
            926,  // yEnd
            80 // wellDiameter
        );

        PlateAnalysisServiceImpl service = new PlateAnalysisServiceImpl();
        List<WellAnalysisResult> results = service.analyzePlate(imagePath, params);

        System.out.println("Analysis complete! Found " + results.size() + " wells analyzed.");
        for (WellAnalysisResult result : results) {
            System.out.println("Well " + result.getRow() + result.getColumn() + ": RGB(" + 
                result.getRedValue() + "," + result.getGreenValue() + "," + result.getBlueValue() + ")");
        }
    }
}