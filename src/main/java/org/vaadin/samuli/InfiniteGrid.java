package org.vaadin.samuli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.HasSize;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.data.renderer.Renderer;
import com.vaadin.flow.data.renderer.TextRenderer;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.templatemodel.TemplateModel;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

@Tag("infinite-grid")
@HtmlImport("src/infinite-grid.html")
public class InfiniteGrid extends PolymerTemplate<InfiniteGrid.ExampleModel> implements HasSize {
  @Id("storage")
  private Div storage;
  private int maxSize = 0;

  private BiFunction<Integer, Integer, String> textGenerator = (x,y) -> null;
  private BiFunction<Integer, Integer, Component> componentGenerator = (x,y) -> null;

  private Renderer<Pair> renderer = new TextRenderer<>(pair -> String.format("(%d, %d)", pair.getX(), pair.getY()));

  public InfiniteGrid() {
    // Set the initial value to the "value" property.

  }

  public void setTextGenerator(BiFunction<Integer, Integer, String> textGenerator) {
    this.textGenerator = textGenerator;
  }

  public void setComponentGenerator(
      BiFunction<Integer, Integer, Component> componentGenerator) {
    this.componentGenerator = componentGenerator;
  }

  public void setValue(String value) {
    getModel().setValue(value);
  }

  @ClientCallable
  public void getContent(String[] stuff) {
    maxSize = Math.max(stuff.length * 4, maxSize);
    if (storage.getElement().getChildCount() > maxSize) {
      for (int i = 0; i < maxSize; i++) {
        storage.getElement().removeChild(0);
      }
    }

    List<Pair> retvalue = Arrays.stream(stuff).map(str -> {
      String[] pair = str.split("_");
      Pair p = new Pair(Integer.valueOf(pair[0]), Integer.valueOf(pair[1]));
      Optional.ofNullable(textGenerator.apply(p.getX(), p.getY())).ifPresent(p::setM);
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

  public void setCellSize(int width, int height) {
    getModel().setCellWidth(width);
    getModel().setCellHeight(height);
  }

  public void setItemCount(int x, int y) {
    getModel().setItemX(x);
    getModel().setItemY(y);
  }

  @ClientCallable
  public void handleClick() {
    System.out.println("clicked");
    final UI ui = UI.getCurrent();
    new Thread(() -> {
      try {
        Thread.sleep(10000);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
      ui.access(() -> whenServerResponds());

    }).start();
  }

  public void whenServerResponds() {
    getElement().callFunction("whenServerResponds");
  }

  /**
   * Template model which defines the single "value" property.
   */
  public interface ExampleModel extends TemplateModel {

    void setValue(String name);

    void setCellWidth(Integer width);

    void setCellHeight(Integer height);

    void setItemX(Integer x);

    void setItemY(Integer y);
  }


  public static class Pair implements Serializable {
    private Integer x;
    private Integer y;
    private String m;

    public Pair() {
    }

    public Pair(Integer x, Integer y) {
      this.x = x;
      this.y = y;
    }

    public Integer getX() {
      return x;
    }

    public void setX(Integer x) {
      this.x = x;
    }

    public Integer getY() {
      return y;
    }

    public void setY(Integer y) {
      this.y = y;
    }

    public String getM() {
      return m;
    }

    public void setM(String m) {
      this.m = m;
    }
  }
}
