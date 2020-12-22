package org.vaadin.samuli;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.router.Route;

@Route("")
public class InfiniteGridDemo extends Div {

  public InfiniteGridDemo() {
    InfiniteGrid colorGrid = new InfiniteGrid();
    colorGrid.setFrozenRows(1);
    colorGrid.setFrozenColumns(1);
    colorGrid.setCellSize(300, 40);
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
    colorGrid.setHeight("500px");
    colorGrid.setWidth("100%");
    add(new TextField("Item count", e -> colorGrid.setItemCount(Integer.parseInt(e.getValue()), Integer.parseInt(e.getValue()))));
    add(new TextField("Cellsize", e -> colorGrid.setCellSize(Integer.parseInt(e.getValue()), 40)));
    add(new TextField("Frozen columns", e -> colorGrid.setFrozenColumns(Integer.parseInt(e.getValue()))));
    add(new TextField("Frozen rows", e -> colorGrid.setFrozenRows(Integer.parseInt(e.getValue()))));
    add(new Button("Refresh", e -> colorGrid.refresh()));
    add(colorGrid);
  }
}
