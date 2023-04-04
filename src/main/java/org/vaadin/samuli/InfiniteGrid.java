package org.vaadin.samuli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.littemplate.LitTemplate;
import com.vaadin.flow.component.template.Id;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Tag("infinite-grid")
@JsModule("./src/infinite-grid.js")
/**
 * InfiniteGrid is a Grid like component that allows arbitrary amount of scrolling in both horizontal and vertical directions.
 * Data for cells are fetched lazily on demand from server. InfiniteGrid is not extending or using Vaadin Grid
 * and does not share features like resizable columns or row selection. It is aimed to provide a way to have arbitrary
 * number of columns without a performance penalty. In practice the max scrollable area depends on browser limitations.
 */
public class InfiniteGrid extends LitTemplate implements HasSize {
    public enum HTMLRenderingHints {
        /**
         * HTML element will have attributes x and y set on client side.
         * Using this has a minor performance penalty.
         */
        WITH_XY_ATTRIBUTES,
        /**
         * Optimization to turn off html support and generate text only content.
         */
        TEXT_ONLY,
        /**
         *
         */
        NORMAL
    }

    private static final int DEFAULT_CELLWIDTH = 200;
    private static final int DEFAULT_CELLHEIGHT = 40;
    private static final int DEFAULT_BUFFER_X = 4;
    private static final int DEFAULT_BUFFER_Y = 10;

    @Id("storage")
    private Div storage;

    private BiFunction<Integer, Integer, String> htmlGenerator = (x, y) -> null;
    private BiFunction<Integer, Integer, Component> componentGenerator = (x, y) -> null;
    private final Dimensions dimensions;

    public InfiniteGrid() {
        dimensions = new Dimensions();
        setCellSize(DEFAULT_CELLWIDTH, DEFAULT_CELLHEIGHT);
        setBufferSize(DEFAULT_BUFFER_X, DEFAULT_BUFFER_Y);
    }

    /**
     * HTML generator to generate cell content with text/html.
     *
     * @param htmlGenerator
     * @param hints Rendering hints to html generation.
     */
    public void setHtmlGenerator(BiFunction<Integer, Integer, String> htmlGenerator,
                                 HTMLRenderingHints hints) {
        this.htmlGenerator = htmlGenerator;
        switch (hints) {
            case WITH_XY_ATTRIBUTES -> {
                getElement().setProperty("setXYAttributes", true);
                getElement().setProperty("textOnly", false);
            }
            case TEXT_ONLY -> {
                getElement().setProperty("setXYAttributes", false);
                getElement().setProperty("textOnly", true);
            }
            case NORMAL -> {
                getElement().setProperty("setXYAttributes", false);
                getElement().setProperty("textOnly", false);
            }
        }
    }

    /**
     * Static Lit template to be used for all cells. x and y coordinates are provided.
     *
     * @param template
     */
    public void setTemplateGenerator(String template) {
        getElement().setProperty("domTemplate", template);
    }

    /**
     * Component generator to generate content with components.
     * Please note that this comes with performance penalty. Use text generator if possible.
     *
     * @param componentGenerator
     */
    public void setComponentGenerator(
            BiFunction<Integer, Integer, Component> componentGenerator) {
        this.componentGenerator = componentGenerator;
    }

    @ClientCallable
    public void getContent(String[] stuff) {
        List<CellData> retvalue = Arrays.stream(stuff).map(str -> {
            String[] pair = str.split("_");
            CellData p = new CellData(Integer.valueOf(pair[0]), Integer.valueOf(pair[1]));
            Optional.ofNullable(htmlGenerator.apply(p.getX(), p.getY())).ifPresent(p::setM);
            Optional.ofNullable(componentGenerator.apply(p.getX(), p.getY())).ifPresent(component -> {
                component.setId("id" + p.getX() + "_" + p.getY());
                removeStorageComponentById(component.getId().orElse(""));
                storage.add(component);
            });
            return p;
        }).collect(Collectors.toList());

        try {
            getElement().callJsFunction("setContent", new ObjectMapper().writeValueAsString(retvalue));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh the content. Use case: When the data changes.
     */
    public void refresh() {
        getElement().callJsFunction("refresh");
    }

    @ClientCallable
    public void removeStorageComponentById(String id) {
        storage.remove(storage.getChildren().filter(existing -> existing.getId().orElse("").equals(id)).toArray(Component[]::new));
    }

    @ClientCallable
    public void cleanStorage() {
        storage.removeAll();
    }

    /**
     * Set cell size in pixels. All cells will be with same size.
     *
     * @param width
     * @param height
     */
    public void setCellSize(int width, int height) {
        dimensions.setCellWidth(width);
        dimensions.setCellHeight(height);
        getElement().setPropertyBean("dimensions", dimensions);
    }

    /**
     * Set the maximum number of cells.
     *
     * @param x number of horizontal cells.
     * @param y number of vertical cells.
     */
    public void setItemCount(int x, int y) {
        dimensions.setCellCountX(x);
        dimensions.setCellCountY(y);
        getElement().setPropertyBean("dimensions", dimensions);
    }

    /**
     * Set the buffer of how many cells in the hidden will component create.
     * Larger the buffer the smoother scrolling but with penalty of having
     * more elements outside visible area.
     *
     * @param x
     * @param y
     */
    public void setBufferSize(int x, int y) {
        getElement().setProperty("bufferX", x);
        getElement().setProperty("bufferY", y);
    }

    /**
     * Set the number of frozen columns starting from left.
     *
     * @param columns
     */
    public void setFrozenColumns(int columns) {
        dimensions.setFrozenColumns(columns);
        getElement().setPropertyBean("dimensions", dimensions);
    }

    /**
     * Set the number of frozen rows starting from top.
     *
     * @param rows
     */
    public void setFrozenRows(int rows) {
        dimensions.setFrozenRows(rows);
        getElement().setPropertyBean("dimensions", dimensions);
    }

    public static class Dimensions {
        private Integer cellWidth;
        private Integer cellHeight;
        private Integer cellCountX;
        private Integer cellCountY;
        private Integer frozenColumns;
        private Integer frozenRows;

        public Integer getCellWidth() {
            return cellWidth;
        }

        public void setCellWidth(Integer cellWidth) {
            this.cellWidth = cellWidth;
        }

        public Integer getCellHeight() {
            return cellHeight;
        }

        public void setCellHeight(Integer cellHeight) {
            this.cellHeight = cellHeight;
        }

        public Integer getCellCountX() {
            return cellCountX;
        }

        public void setCellCountX(Integer cellCountX) {
            this.cellCountX = cellCountX;
        }

        public Integer getCellCountY() {
            return cellCountY;
        }

        public void setCellCountY(Integer cellCountY) {
            this.cellCountY = cellCountY;
        }

        public Integer getFrozenColumns() {
            return frozenColumns;
        }

        public void setFrozenColumns(Integer frozenColumns) {
            this.frozenColumns = frozenColumns;
        }

        public Integer getFrozenRows() {
            return frozenRows;
        }

        public void setFrozenRows(Integer frozenRows) {
            this.frozenRows = frozenRows;
        }
    }

    private static class CellData implements Serializable {
        private Integer x;
        private Integer y;
        private String m;

        public CellData(Integer x, Integer y) {
            this.x = x;
            this.y = y;
        }

        public Integer getX() {
            return x;
        }

        public Integer getY() {
            return y;
        }

        public String getM() {
            return m;
        }

        public void setM(String m) {
            this.m = m;
        }
    }
}
