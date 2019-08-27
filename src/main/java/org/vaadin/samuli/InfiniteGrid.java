package org.vaadin.samuli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.templatemodel.TemplateModel;
import org.vaadin.samuli.InfiniteGrid.InfiniteGridModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Tag("infinite-grid")
@HtmlImport("src/infinite-grid.html")
public class InfiniteGrid extends PolymerTemplate<InfiniteGridModel> implements HasSize {
  @Id("storage")
  private Div storage;
  private int maxSize = 0;

  private BiFunction<Integer, Integer, String> htmlGenerator = (x,y) -> null;
  private BiFunction<Integer, Integer, Component> componentGenerator = (x,y) -> null;

  private Renderer<CellData> renderer = new TextRenderer<>(pair -> String.format("(%d, %d)", pair.getX(), pair.getY()));

  public InfiniteGrid() {
  }

  /**
   * HTML generator to generate cell content with text/html.
   * x and y coordinates are provided as polymer data model like [[x]] and [[y]]
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
    getModel().setCellWidth(width);
    getModel().setCellHeight(height);
  }

  /**
   * Set the maximum number of cells.
   * @param x number of horizontal cells.
   * @param y number of vertical cells.
   */
  public void setItemCount(int x, int y) {
    getModel().setCellCountX(x);
    getModel().setCellCountY(y);
  }

  /**
   * Template model which defines the single "value" property.
   */
  public interface InfiniteGridModel extends TemplateModel {
    void setCellWidth(Integer width);
    void setCellHeight(Integer height);

    void setCellCountX(Integer x);
    void setCellCountY(Integer y);
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
