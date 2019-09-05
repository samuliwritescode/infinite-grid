package org.vaadin.samuli;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class InfiniteGridDemo extends Div {

  public InfiniteGridDemo() {
    InfiniteGrid colorGrid = new InfiniteGrid();
    colorGrid.setFrozenRows(1);
    colorGrid.setFrozenColumns(1);
    colorGrid.setCellSize(200, 40);
    colorGrid.setItemCount(100, 100);
    colorGrid.setHtmlGenerator((x, y) -> {
      if (y<1) {
        return "column"+x;
      }

      if (x < 1) {
        return "line"+y;
      }
      return x+", "+y;
    });

    setSizeFull();
    colorGrid.setSizeFull();
    getElement().getStyle().set("overflow", "hidden");
    add(colorGrid);
  }
}
