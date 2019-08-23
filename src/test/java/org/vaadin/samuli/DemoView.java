package org.vaadin.samuli;

import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("")
public class DemoView extends Div {

  public DemoView() {
    InfiniteGrid infiniteGrid = new InfiniteGrid();
    infiniteGrid.setWidth("100%");
    infiniteGrid.setHeight("50%");
    infiniteGrid.setCellSize(200, 40);
    infiniteGrid.setItemCount(100000, 100000);
    add(infiniteGrid);
    Grid<Object> grid = new Grid<>();
    grid.setDataProvider(new ListDataProvider<Object>(IntStream.range(0, 100000).mapToObj(i -> i).collect(Collectors.toList())));
    grid.setWidth("100%");
    grid.setHeight("50%");
    for (int loop=0; loop < 9; loop++) grid.addColumn(TemplateRenderer.of(""+(loop+1)));
    add(grid);
    setSizeFull();
  }
}
