package mdp.mdp_android;

/**
 * Created by andreaschrisnamayong on 9/16/16.
 */
public class Map {
    private static final int MAP_WIDTH = 15;
    private static final int MAP_LENGTH = 20;
    private static int[][] mapData = new int[MAP_WIDTH][MAP_LENGTH];
    private static int start_x = 0;
    private static int start_y = 0;

    public Map() {

    }

    public Map(int x, int y) {
        start_x = x;
        start_y = y;
    }

    public static void resetMap() {
        for (int i=0; i<MAP_WIDTH; i++) {
            for (int j=0; j<MAP_LENGTH; j++) {
                mapData[i][j] = 0;
            }
        }
    }

    public void setObstacle(int x, int y) {
        if ((x > -1 && x < 3 && y > -1 && y < 3) || (x < 15 && x > 11 && y < 15 && y > 11)) {

        } else {
            mapData[x][y] = -1;
        }

    }

    public void setDiscovered(int x, int y) {
        if (x > -1 && y > -1 && x < mapData.length && y < mapData[x].length) {
            mapData[x][y] = 1;
        }
    }

    public int getMapLength() {
        return MAP_LENGTH;
    }

    public int getMapWidth() {
        return MAP_WIDTH;
    }

    public int[][] getMapData() {
        return mapData;
    }
}
