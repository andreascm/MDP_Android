package mdp.mdp_android;

/**
 * Created by andreaschrisnamayong on 9/16/16.
 */
public class Map {
    private static final int MAP_WIDTH = 15;
    private static final int MAP_LENGTH = 40;
    private static int[][] map = new int[MAP_WIDTH][MAP_LENGTH];
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
            for (int j=0; i<MAP_LENGTH; i++) {
                map[i][j] = 0;
            }
        }
    }

    public void setObstacle(int x1, int x2, int y1, int y2) {
        map[x1][y1] = -1;
        map[x2][y2] = -1;
        map[x1][y2] = -1;
        map[x2][y1] = -1;
    }

    public void setDiscovered(int x, int y) {
        map[x][y] = 1;
    }
    public int getMapLength() {
        return MAP_LENGTH;
    }

    public int getMapWidth() {
        return MAP_WIDTH;
    }
}
