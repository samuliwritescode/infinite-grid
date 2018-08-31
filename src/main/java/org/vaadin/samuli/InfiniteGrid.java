package org.vaadin.samuli;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.vaadin.flow.component.ClientCallable;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.Tag;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.dependency.HtmlImport;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.polymertemplate.Id;
import com.vaadin.flow.component.polymertemplate.PolymerTemplate;
import com.vaadin.flow.templatemodel.TemplateModel;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Tag("infinite-grid")
@HtmlImport("src/infinite-grid.html")
public class InfiniteGrid extends PolymerTemplate<InfiniteGrid.ExampleModel> {
    @Id("storage")
    private Div storage;

    /**
     * Template model which defines the single "value" property.
     */
    public interface ExampleModel extends TemplateModel {

        void setValue(String name);
    }

    public InfiniteGrid() {
        // Set the initial value to the "value" property.

    }

    public void setValue(String value) {
        getModel().setValue(value);
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

    private int maxSize = 0;

    @ClientCallable
    public void getContent(String[] stuff) {
        maxSize = Math.max(stuff.length, maxSize);
        System.out.println("max size "+maxSize);
        if (storage.getElement().getChildCount() > maxSize) {
            for(int i=0; i < maxSize;i++) {
//                storage.getElement().removeChild(0);
            }
        }
//        storage.removeAll();
        List<Pair> retvalue = Arrays.stream(stuff).map(str -> {
            String[] pair = str.split(":");
            Pair p = new Pair(Integer.valueOf(pair[0]), Integer.valueOf(pair[1]));
            p.setM(String.format("(%d, %d)", p.getX(), p.getY()));
            Component c = ((p.getX()+p.getY())%2) == 0 ?new Button(p.getM(), l-> System.out.println("got "+p.getM())):new Label(p.getM());
            c.setId("id"+p.getX()+"_"+p.getY());
            storage.add(c);
            return p;
        }).collect(Collectors.toList());

        try {
            getElement().callFunction("setContent", new ObjectMapper().writeValueAsString(retvalue));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
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
}
