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
    InfiniteGrid htmlGrid = createInfiniteGrid();
    htmlGrid.setHtmlGenerator(
        (x, y) -> "<button onClick=\"function kek(){this.dispatchEvent(new CustomEvent('cldick',  {detail: {kek: 1}}));}; kek();\">[[x]], [[y]]</button>");
    htmlGrid.getElement().addEventListener("cldick", l -> {
      System.out.println("click " + l.getEventData());
    });
    add(htmlGrid);

    InfiniteGrid componentGrid = createInfiniteGrid();
    componentGrid.setComponentGenerator((x, y) ->
        new Button(
            String.format("(%d, %d)", x, y),
            e -> Notification.show(String.format("clicked (%d, %d)", x, y))
        ));
    add(componentGrid);
    setSizeFull();
  }

  private InfiniteGrid createInfiniteGrid() {
    InfiniteGrid infiniteGrid = new InfiniteGrid();
    infiniteGrid.getElement().getClassList().add("borders");
    infiniteGrid.setWidth("100%");
    infiniteGrid.setHeight("50%");
    infiniteGrid.setCellSize(200, 40);
    infiniteGrid.setItemCount(100, 100);
    return infiniteGrid;
  }
}
