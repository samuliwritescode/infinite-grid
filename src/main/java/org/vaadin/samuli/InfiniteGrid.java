package org.vaadin.samuli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.dependency.JsModule;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.vaadin.samuli.InfiniteGrid.InfiniteGridModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Tag("infinite-grid")
@JsModule("./src/infinite-grid.js")
@HtmlImport("src/infinite-grid.html")
/**
  * InfiniteGrid is a Grid like component that allows arbitrary amount of scrolling in both horizontal and vertical directions.
  * Data for cells are fetched lazily on demand from server. InfiniteGrid is not extending or using Vaadin Grid
  * and does not share features like resizable columns or row selection. It is aimed to provide a way to have arbitrary
  * number of columns without a performance penalty. In practice the max scrollable area depends on browser limitations.
 */
public class InfiniteGrid extends PolymerTemplate<InfiniteGridModel> implements HasSize {
  private static final int DEFAULT_CELLWIDTH = 200;
  private static final int DEFAULT_CELLHEIGHT = 40;

  @Id("storage")
  private Div storage;
  private int maxSize = 0;

  private BiFunction<Integer, Integer, String> htmlGenerator = (x,y) -> null;
  private BiFunction<Integer, Integer, Component> componentGenerator = (x,y) -> null;

  public InfiniteGrid() {
    setCellSize(DEFAULT_CELLWIDTH, DEFAULT_CELLHEIGHT);
    setUseDomBind(false);
    setTextOnly(false);
  }

  /**
   * HTML generator to generate cell content with text/html.
   * When setUseDomBind is set to true x and y coordinates
   * are provided as polymer data model like [[x]] and [[y]]
   * @param textGenerator
   */
  public void setHtmlGenerator(BiFunction<Integer, Integer, String> textGenerator) {
    this.htmlGenerator = textGenerator;
  }

  /**
   * Component generator to generate content with components.
   * Please note that this comes with performance penalty. Use text generator if possible.
   * @param componentGenerator
   */
  public void setComponentGenerator(
      BiFunction<Integer, Integer, Component> componentGenerator) {
    this.componentGenerator = componentGenerator;
  }

  @ClientCallable
  public void getContent(String[] stuff) {
    maxSize = Math.max(stuff.length * 4, maxSize);
    if (storage.getElement().getChildCount() > maxSize) {
      for (int i = 0; i < maxSize; i++) {
        storage.getElement().removeChild(0);
      }
    }

    List<CellData> retvalue = Arrays.stream(stuff).map(str -> {
      String[] pair = str.split("_");
      CellData p = new CellData(Integer.valueOf(pair[0]), Integer.valueOf(pair[1]));
      Optional.ofNullable(htmlGenerator.apply(p.getX(), p.getY())).ifPresent(p::setM);
      Optional.ofNullable(componentGenerator.apply(p.getX(), p.getY())).ifPresent(component -> {
        component.setId("id" + p.getX() + "_" + p.getY());
        storage.add(component);
      });
      return p;
    }).collect(Collectors.toList());

    try {
      getElement().callFunction("setContent", new ObjectMapper().writeValueAsString(retvalue));
    } catch (JsonProcessingException e) {
      e.printStackTrace();
    }
  }

  /**
   * Set cell size in pixels. All cells will be with same size.
   * @param width
   * @param height
   */
  public void setCellSize(int width, int height) {
    getModel().getDimensions().setCellWidth(width);
    getModel().getDimensions().setCellHeight(height);
  }

  /**
   * Set the maximum number of cells.
   * @param x number of horizontal cells.
   * @param y number of vertical cells.
   */
  public void setItemCount(int x, int y) {
    getModel().getDimensions().setCellCountX(x);
    getModel().getDimensions().setCellCountY(y);
  }

  /**
   * Whether or not html generator supports polymer data model or not.
   * By default it does not. Having this off is a small optimization.
   * @param use
   */
  public void setUseDomBind(boolean use) {
    getModel().setUseDomBind(use);
  }

  /**
   * Optimization to turn off html support and generate text only content.
   * @param textOnly
   */
  public void setTextOnly(boolean textOnly) {
    getModel().setTextOnly(textOnly);
  }

  /**
   * Settings shared with client side.
   */
  public interface InfiniteGridModel extends TemplateModel {
    void setDimensions(Dimensions dimensions);
    Dimensions getDimensions();

    void setUseDomBind(Boolean use);
    void setTextOnly(Boolean textonly);
  }

  public static class Dimensions {
    private Integer cellWidth;
    private Integer cellHeight;
    private Integer cellCountX;
    private Integer cellCountY;

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
