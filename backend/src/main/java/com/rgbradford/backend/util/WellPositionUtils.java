package com.rgbradford.backend.util;

public class WellPositionUtils {
    
    /**
     * Converts row and column indices to a well position string (e.g., 0,0 -> "A1")
     * @param row 0-based row index
     * @param column 1-based column index (since columns typically start at 1 in plate readers)
     * @return The well position as a string (e.g., "A1", "B2")
     */
    public static String toPosition(int row, int column) {
        return String.format("%s%d", (char)('A' + row), column + 1);
    }
    
    /**
     * Converts a well position string to row and column indices
     * @param position The well position (e.g., "A1", "B2")
     * @return An array where [0] is row index and [1] is column index (both 0-based)
     * @throws IllegalArgumentException if the position string is invalid
     */
    public static int[] fromPosition(String position) {
        if (position == null || position.length() < 2) {
            throw new IllegalArgumentException("Invalid well position: " + position);
        }
        
        // Extract letters part (can be multiple letters for >26 rows, e.g., AA, AB, etc.)
        int i = 0;
        while (i < position.length() && Character.isLetter(position.charAt(i))) {
            i++;
        }
        
        if (i == 0) {
            throw new IllegalArgumentException("No letters found in well position: " + position);
        }
        
        String letters = position.substring(0, i).toUpperCase();
        String numberStr = position.substring(i);
        
        try {
            // Convert letters to row number (0-based)
            int row = 0;
            for (int j = 0; j < letters.length(); j++) {
                row = row * 26 + (letters.charAt(j) - 'A' + 1);
            }
            row--; // Convert to 0-based
            
            // Convert number to column number (0-based)
            int col = Integer.parseInt(numberStr) - 1;
            
            return new int[]{row, col};
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid number in well position: " + position, e);
        }
    }
    
    /**
     * Gets the row letter(s) for a given 0-based row index
     * @param row 0-based row index
     * @return The row letter(s) (e.g., 0 -> "A", 26 -> "AA")
     */
    public static String getRowLetter(int row) {
        StringBuilder sb = new StringBuilder();
        row++; // Convert to 1-based for calculation
        
        while (row > 0) {
            row--; // Adjust to 0-25 for 'A' to 'Z'
            char c = (char)('A' + (row % 26));
            sb.insert(0, c);
            row = row / 26;
        }
        
        return sb.toString();
    }
    
    /**
     * Gets the 1-based column number for display
     * @param column 0-based column index
     * @return 1-based column number
     */
    public static int getColumnNumber(int column) {
        return column + 1;
    }
}
