package mdp.mdp_android;

/**
 * Created by andreaschrisnamayong on 9/16/16.
 */
public class Robot {
    public static final int UP = 1;
    public static final int DOWN = 2;
    public static final int LEFT = 3;
    public static final int RIGHT = 4;

    public static final int MOVE_FORWARD = 5;
    public static final int MOVE_BACKWARD = 6;
    public static final int TURN_LEFT = 7;
    public static final int TURN_RIGHT = 8;

    public static int current_x = 0;
    public static int current_y = 0;
    public static int direction = UP;
    public static Map map = new Map();

    public Robot() {

    }

    public Robot(Map map, int x, int y, int moveDirection) {
        this.map = map;
        current_x = x;
        current_y = y;
        direction = moveDirection;
    }

    public Map setRobot(int x, int y) {
        current_x = x;
        current_y = y;
        map.setDiscovered(x, y);
        discoverSurrounding();
        return map;
    }

    public void moveForward() {
        updatePosition(MOVE_FORWARD);
    }

    public void moveBackward() {
        updatePosition(MOVE_BACKWARD);
    }

    public void turnLeft() {
        updatePosition(TURN_LEFT);
    }

    public void turnRight() {
        updatePosition(TURN_RIGHT);
    }

    public Map discoverSurrounding() {
        map.setDiscovered(current_x, current_y);
        if (current_x > -1 && current_y > -1) {

            if (current_y > 0) {
                map.setDiscovered(current_x, current_y-1);
            }
            if (current_y < map.getMapLength()-1) {
                map.setDiscovered(current_x, current_y+1);
            }
        }
        if (current_x > 0) {
            if (current_y > 0) {
                map.setDiscovered(current_x-1, current_y-1);
            }
            map.setDiscovered(current_x-1, current_y);
            if (current_y < map.getMapLength()-1) {
                map.setDiscovered(current_x-1, current_y+1);
            }
        }
        if (current_x < map.getMapWidth()-1) {
            if (current_y > 0) {
                map.setDiscovered(current_x+1, current_y-1);
            }
            map.setDiscovered(current_x+1, current_y);
            if (current_y < map.getMapLength()-1) {
                map.setDiscovered(current_x+1, current_y+1);
            }
        }
        return map;
    }

    public void updatePosition(int action) {
        if (action == MOVE_FORWARD) {
            if (direction == UP) {
                current_y += 1;
            } else if (direction == DOWN) {
                current_y -= 1;
            } else if (direction == LEFT) {
                current_x -= 1;
            } else if (direction == RIGHT) {
                current_x += 1;
            }
        } else if (action == MOVE_BACKWARD) {
            if (direction == UP) {
                current_y -= 1;
            } else if (direction == DOWN) {
                current_y += 1;
            } else if (direction == LEFT) {
                current_x += 1;
            } else if (direction == RIGHT) {
                current_x -= 1;
            }
        } else if (action == TURN_LEFT) {
            if (direction == UP) {
                direction = LEFT;
            } else if (direction == DOWN) {
                direction = RIGHT;
            } else if (direction == LEFT) {
                direction = DOWN;
            } else if (direction == RIGHT) {
                direction = UP;
            }
        } else if (action == TURN_RIGHT) {
            if (direction == UP) {
                direction = RIGHT;
            } else if (direction == DOWN) {
                direction = LEFT;
            } else if (direction == LEFT) {
                direction = UP;
            } else if (direction == RIGHT) {
                direction = DOWN;
            }
        }
    }

    public int getCurrentX() {
        return current_x;
    }

    public int getCurrentY() {
        return current_y;
    }

    public int getDirection() {
        return direction;
    }
}
