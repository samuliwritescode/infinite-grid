package org.vaadin.samuli;

import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.router.Route;

@Route("")
public class InfiniteGridDemo extends Div {

  public InfiniteGridDemo() {
    InfiniteGrid colorGrid = new InfiniteGrid();
    colorGrid.setCellSize(100, 100);
    colorGrid.setItemCount(1000, 1000);
    colorGrid.setHtmlGenerator((x, y) -> {
      return "<div style=\"width: 100%; height:100%; background-color: #" +
          String.format("%02x%02x%02x",
              Math.abs((int) (Math.sin(x * 0.3) * 255)),
              Math.abs((int) (Math.sin(x * 0.2) * 127 + Math.cos(y * 0.2) * 127)),
              Math.abs((int) (Math.cos(y * 0.3) * 255))) +
          ";\"></div>";
    });

    setSizeFull();
    colorGrid.setSizeFull();
    getElement().getStyle().set("overflow", "hidden");
    add(colorGrid);
  }
}
