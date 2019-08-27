package org.vaadin.samuli;

import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.renderer.TemplateRenderer;
import com.vaadin.flow.router.Route;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Route("")
@HtmlImport("src/shared-styles.html")
public class DemoView extends Div {

  public DemoView() {
    InfiniteGrid infiniteGrid = new InfiniteGrid();
    infiniteGrid.getElement().getClassList().add("borders");
    infiniteGrid.setWidth("100%");
    infiniteGrid.setHeight("50%");
    infiniteGrid.setCellSize(200, 40);
    infiniteGrid.setItemCount(100, 100);
    infiniteGrid.setTextGenerator(
        (x, y) -> "<button onClick=\"function kek(){this.dispatchEvent(new CustomEvent('cldick',  {detail: {kek: 1}}));}; kek();\">[[x]], [[y]]</button>");
    infiniteGrid.getElement().addEventListener("cldick", l -> {
      System.out.println("click " + l.getEventData());
    });
    //    infiniteGrid.setComponentGenerator((x,y) -> new Button(String.format("(%d, %d)", x, y)));
    add(infiniteGrid);

    Grid<Object> grid = new Grid<>();
    grid.setDataProvider(new ListDataProvider<>(IntStream.range(0, 100).mapToObj(i -> i).collect(Collectors.toList())));
    grid.setWidth("100%");
    grid.setHeight("50%");
    for (int loop = 0; loop < 9; loop++)
    //      grid.addComponentColumn(o ->new Button(o.toString()));
    {
      grid.addColumn(TemplateRenderer.of("" + (loop + 1)).withEventHandler("click", o -> {

      }));
    }
    add(grid);
    setSizeFull();
  }
}
