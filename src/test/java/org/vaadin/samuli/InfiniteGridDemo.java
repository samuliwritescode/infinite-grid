package org.vaadin.samuli;

import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;

@Route("")
@HtmlImport("src/shared-styles.html")
public class InfiniteGridDemo extends VerticalLayout {

  public InfiniteGridDemo() {
    HorizontalLayout firstRow = new HorizontalLayout();
    HorizontalLayout secondRow = new HorizontalLayout();
    firstRow.setSizeFull();
    secondRow.setSizeFull();
    firstRow.setSpacing(false);
    secondRow.setSpacing(false);
    firstRow.setMargin(false);
    secondRow.setMargin(false);

    InfiniteGrid textGrid = createInfiniteGrid();
    textGrid.setTextOnly(true);
    textGrid.setHtmlGenerator((x,y)-> String.format("%d, %d", x,y));
    firstRow.add(textGrid);

    InfiniteGrid htmlGrid = createInfiniteGrid();
    htmlGrid.setUseDomBind(true);
    htmlGrid.setHtmlGenerator((x, y) -> "<button> [[x]], [[y]]</button>");
    firstRow.add(htmlGrid);

    InfiniteGrid componentGrid = createInfiniteGrid();
    componentGrid.setComponentGenerator((x, y) ->
        new Button(
            String.format("%d, %d", x, y),
            e -> Notification.show(String.format("clicked (%d, %d)", x, y))
        ));
    secondRow.add(componentGrid);

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
    colorGrid.setHeight("100%");
    secondRow.add(colorGrid);
    setSizeFull();
    setSpacing(true);
    setMargin(false);
    setPadding(false);
    add(
        new H3(
            "Below there are 4 InfiniteGrids. 1. has html only content. 2. has html with data model. 3. has Vaadin components. 4. has colorful content as a show off."
        ),
        firstRow,
        secondRow
    );
  }

  private InfiniteGrid createInfiniteGrid() {
    InfiniteGrid infiniteGrid = new InfiniteGrid();
    infiniteGrid.getElement().getClassList().add("borders");
    infiniteGrid.setWidth("50%");
    infiniteGrid.setHeight("100%");
    infiniteGrid.setCellSize(200, 40);
    infiniteGrid.setItemCount(100000, 100000);
    return infiniteGrid;
  }
}
