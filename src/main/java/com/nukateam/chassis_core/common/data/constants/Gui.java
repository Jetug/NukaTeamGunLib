package com.nukateam.chassis_core.common.data.constants;

import com.nukateam.chassis_core.common.util.Pos2I;

import java.awt.*;

public class Gui {
    public static final int HOTBAR_SLOT_COUNT = 9;
    public static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    public static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    public static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    public static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    public static final int VANILLA_FIRST_SLOT_INDEX = 0;
    public static final int INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    public static final int TAB_HEIGHT = 27;
    public static final int TAB_WIDTH = 29;

    public static final Pos2I TOP_TAB_ICON_POS_1 = new Pos2I(6, -20);
    public static final Pos2I TOP_TAB_ICON_POS_2 = new Pos2I(35, -20);

    public static final Pos2I BOTTOM_TAB_ICON_POS_1 = new Pos2I(6, -20);
    public static final Pos2I BOTTOM_TAB_ICON_POS_2 = new Pos2I(35, -20);

    public static final Pos2I HEAD_SLOT_POS = new Pos2I(68, 8);
    public static final Pos2I BODY_SLOT_POS = new Pos2I(68, 44);
    public static final Pos2I LEFT_ARM_SLOT_POS = new Pos2I(59, 26);
    public static final Pos2I RIGHT_ARM_SLOT_POS = new Pos2I(77, 26);
    public static final Pos2I LEFT_LEG_SLOT_POS = new Pos2I(59, 62);
    public static final Pos2I RIGHT_LEG_SLOT_POS = new Pos2I(77, 62);
    public static final Pos2I ENGINE_SLOT_POS = new Pos2I(98, 26);

    public static final Pos2I FRAME_BODY_SLOT_POS = new Pos2I(8, 17);
    public static final Pos2I FRAME_LEFT_ARM_SLOT_POS = new Pos2I(80, 35);
    public static final Pos2I FRAME_RIGHT_ARM_SLOT_POS = new Pos2I(8, 35);
    public static final Pos2I FRAME_LEFT_LEG_SLOT_POS = new Pos2I(80, 71);
    public static final Pos2I FRAME_RIGHT_LEG_SLOT_POS = new Pos2I(8, 71);
    public static final Pos2I LEFT_HAND_SLOT_POS = new Pos2I(80, 53);
    public static final Pos2I RIGHT_HAND_SLOT_POS = new Pos2I(8, 53);
    public static final Pos2I BACK_SLOT_POS = new Pos2I(152, 17);
    public static final Pos2I ENGINE_SLOT_POS2 = new Pos2I(152, 35);
    public static final Pos2I COOLING_SLOT_POS = new Pos2I(152, 71);

    public static final Rectangle HEAD_ICON_OFFSET = createStandardIcon(19, 0);
    public static final Rectangle BODY_ICON_OFFSET = createStandardIcon(19, 17);
    public static final Rectangle LEFT_ARM_ICON_OFFSET = createStandardIcon(36, 0);
    public static final Rectangle RIGHT_ARM_ICON_OFFSET = createStandardIcon(53, 0);
    public static final Rectangle LEFT_LEG_ICON_OFFSET = createStandardIcon(36, 17);
    public static final Rectangle RIGHT_LEG_ICON_OFFSET = createStandardIcon(53, 17);

    private static Rectangle createStandardIcon(int x, int y) {
        return new Rectangle(x, y, 16, 16);
    }
}
