package element;

import element.object.BasicObject;

public class Port {
    public static final int VISUAL_SIZE = 14;
    public static final int HIT_RADIUS = VISUAL_SIZE / 2 + 4; 

    private final BasicObject owner;
    private int offsetX; // 相對於 owner 左上角的偏移量
    private int offsetY;

    public Port(BasicObject owner, int offsetX, int offsetY) {
        this.owner   = owner;
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    // 絕對座標動態計算，owner 移動後自動更新
    public int getX() { return owner.getX() + offsetX; }
    public int getY() { return owner.getY() + offsetY; }

    public boolean isInRange(int mx, int my) {
         return Math.abs(getX() - mx) <= HIT_RADIUS &&
             Math.abs(getY() - my) <= HIT_RADIUS;
    }

    public int getOffsetX() {
        return offsetX;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffset(int offsetX, int offsetY) {
        this.offsetX = offsetX;
        this.offsetY = offsetY;
    }

    public BasicObject getOwner() { return owner; }
}