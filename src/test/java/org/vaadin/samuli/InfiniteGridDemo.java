package org.vaadin.samuli;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.router.Route;

@Route("")
@HtmlImport("src/shared-styles.html")
public class InfiniteGridDemo extends Div {

  public InfiniteGridDemo() {
    InfiniteGrid textGrid = createInfiniteGrid();
    textGrid.setHtmlGenerator((x,y)-> String.format("%d, %d", x,y));
    add(textGrid);

    InfiniteGrid htmlGrid = createInfiniteGrid();
    htmlGrid.setUseDomBind(true);
    htmlGrid.setHtmlGenerator((x, y) -> "<button> [[x]], [[y]]</button>");
    add(htmlGrid);

    InfiniteGrid componentGrid = createInfiniteGrid();
    componentGrid.setComponentGenerator((x, y) ->
        new Button(
            String.format("(%d, %d)", x, y),
            e -> Notification.show(String.format("clicked (%d, %d)", x, y))
        ));
    add(componentGrid);

    InfiniteGrid colorGrid = new InfiniteGrid();
    colorGrid.setCellSize(100,100);
    colorGrid.setItemCount(1000, 1000);
    colorGrid.setHtmlGenerator((x,y) -> {
      return "<div style=\"width: 100%; height:100%; background-color: #" +
          String.format("%02x%02x%02x",
              Math.abs((int)(Math.sin(x*0.3)*255)),
              Math.abs((int)(Math.sin(x*0.2)*127 + Math.cos(y*0.2)*127)),
              Math.abs((int)(Math.cos(y*0.3)*255))) +
          ";\"></div>";
    });
    colorGrid.setWidth("50%");
    colorGrid.setHeight("45%");
    add(colorGrid);
    setSizeFull();

    getElement().getStyle().set("overflow", "hidden");
  }

  private InfiniteGrid createInfiniteGrid() {
    InfiniteGrid infiniteGrid = new InfiniteGrid();
    infiniteGrid.getElement().getClassList().add("borders");
    infiniteGrid.setWidth("50%");
    infiniteGrid.setHeight("45%");
    infiniteGrid.setCellSize(200, 40);
    infiniteGrid.setItemCount(100, 100);
    return infiniteGrid;
  }
}
