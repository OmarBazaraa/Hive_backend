package models.map;

import models.map.base.Grid;


/**
 * This {@code GuideGrid} class represents the guide map of {@code DstHiveObject}.
 */
public class GuideGrid extends Grid<GuideCell> {

    //
    // Member Methods
    //

    /**
     * Constructs a new grid.
     *
     * @param grid the grid configurations.
     */
    public GuideGrid(GuideCell[][] grid) {
        super(grid);
    }
}
