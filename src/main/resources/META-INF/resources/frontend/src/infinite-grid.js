import { PolymerElement } from '@polymer/polymer/polymer-element.js';
import { html } from '@polymer/polymer/lib/utils/html-tag.js';

class InfiniteGrid extends PolymerElement {
  static get template() {
    return html`
   <style include="shared-styles">
            :host {
                display: inline-block;
                padding: 0;
                margin: 0;
                position: relative;
            }

            .scrollarea {
                position: absolute;
                top: 0;
                right: 0;
                bottom: 0;
                left: 0;
                padding: 0;
                margin: 0;
                overflow: auto;
            }

            .container {
                padding: 0;
                margin: 0;
                overflow: hidden;
                position: relative;
            }

            .storage {
                display: none;
            }

            .row {
                display: block;
                white-space: nowrap;
                position: absolute;
            }

            .cell {
                overflow: hidden;
                display: inline-block;
                box-sizing: border-box;
                position: absolute;
            }
        </style> 
   <div id="scrollarea" class="scrollarea"> 
    <div id="container" class="container"></div> 
   </div> 
   <div id="storage" class="storage"></div> 
`;
  }

  static get is() {
      return 'infinite-grid'
  }

  ready() {
      super.ready();
      this.bufferX = 1;
      this.bufferY = 14;
      var that = this;

      this.$.scrollarea.addEventListener('scroll', pos => setTimeout(() => that.onScroll(), 1));
  }

  static get properties() {
      return {
          dimensions: {type: Object, observer: 'dimensionsChanged'},
          textOnly: {type: Boolean},
          useDomBind: {type: Boolean}
      }
  }

  dimensionsChanged(dimensions) {
      var that = this;
      this.cellWidth = dimensions.cellWidth;
      this.cellHeight = dimensions.cellHeight;

      this.$.container.style.width = dimensions.cellCountX*this.cellWidth+"px";
      this.$.container.style.height = dimensions.cellCountY*this.cellHeight+"px";
      setTimeout(() => that.drawGrid(0, 0), 100);
  }

  setContent(json) {
      let that = this;
      JSON.parse(json).forEach(celldata => {
          setTimeout(() => {
              let cell = this.$.container.querySelector("#cid"+celldata.x+"_"+celldata.y);
              if (cell != null) {
                  if (celldata.m != null) {
                      if (this.useDomBind == true) {
                          var dbind = document.createElement('dom-bind');
                          var template = document.createElement('template');
                          dbind.appendChild(template);
                          template.innerHTML = celldata.m;
                          this.clearChildren(cell);
                          dbind.x = celldata.x;
                          dbind.y = celldata.y;
                          cell.appendChild(dbind);
                      } else if(this.textOnly == true) {
                          cell.innerText = celldata.m;
                      } else {
                          cell.innerHTML = celldata.m;
                      }
                  } else {
                      let c = this.$.storage.querySelector("#id"+celldata.x+"_"+celldata.y);
                      if (c != null) {
                          this.clearChildren(cell);
                          cell.appendChild(c);
                      } else {
                          console.info("cannot find "+celldata.x+"_"+celldata.y);
                      }
                  }
              } else {
                  console.info("no cid"+celldata.x+"_"+celldata.y)
              }
          }, 1);
      });
  }

  drawGrid(initialX, initialY) {
      this.prePosX = initialX;
      this.prePosY = initialY;
      let ask = [];
      this.maxY = Math.ceil(this.$.scrollarea.offsetHeight/this.cellHeight)+this.bufferY;
      this.maxX = Math.ceil(this.$.scrollarea.offsetWidth/this.cellWidth)+this.bufferX;
      //Clear possible old nodes
      this.clearChildren(this.$.container);

      for (var y = initialY; y < this.maxY+initialY; y++) {
          let row = document.createElement("div");
          row.classList.add("row")
          row.style.top = ((y-this.bufferY/2)*this.cellHeight)+"px";
          this.$.container.appendChild(row);

          for (var x = initialX; x < this.maxX+initialX; x++) {
              let span = document.createElement('span');
              let coord = x+"_"+(y-this.bufferY/2);
              span.id = "cid"+coord;
              ask.push(coord);
              span.classList.add('cell');
              span.style.width = this.cellWidth+"px";
              span.style.height = this.cellHeight+"px";
              row.appendChild(span);
              span.style.left = (x*this.cellWidth)+"px";
          }
      }
      this.$server.getContent(ask);
  }

  clearChildren(node) {
      while(node.firstChild) {
          node.removeChild(node.firstChild);
      }
  }

  prepareAskContent(coord, col, ask) {
      col.id = "cid"+coord;
      col.childNodes.forEach(child => col.removeChild(child));
      ask.push(coord);
  }

  isRedrawingMoreEfficient(changeX, changeY) {
      return Math.abs(changeY) > (Math.ceil(this.$.scrollarea.offsetHeight/this.cellHeight)+this.bufferY)/2 ||
      Math.abs(changeX) > (Math.ceil(this.$.scrollarea.offsetWidth/this.cellWidth)+this.bufferX)/2;
  }

  onScroll() {
      let currX = Math.floor(this.$.scrollarea.scrollLeft/this.cellWidth);
      let currY = Math.floor(this.$.scrollarea.scrollTop/this.cellHeight);
      let changeY = currY - this.prePosY;
      let changeX = currX - this.prePosX;
      var ask = [];

      if (this.isRedrawingMoreEfficient(changeX, changeY) === true) {
          this.drawGrid(currX, currY);
          return;
      }

      for (var i = 0; i < Math.abs(changeY) && Math.abs(changeY) >= Math.floor(this.bufferY/2); i++) {
          this.prePosY = currY;
          if (changeY > 0) {
              let first = this.$.container.firstChild;
              let last = this.$.container.lastChild;
              this.$.container.removeChild(first);
              this.$.container.appendChild(first);
              let y = Math.floor((last.offsetTop + this.cellHeight)/this.cellHeight);
              first.style.top = (y*this.cellHeight) + "px";
              first.childNodes.forEach(col => {
                  let coord = Math.floor(col.offsetLeft/this.cellWidth)+"_"+ y;
                  this.prepareAskContent(coord, col, ask);
              });
          } else {
              let last = this.$.container.lastChild;
              let first = this.$.container.firstChild;
              this.$.container.removeChild(last);
              this.$.container.insertBefore(last, first);
              let y = Math.floor((first.offsetTop - this.cellHeight)/this.cellHeight);
              last.style.top = (y*this.cellHeight) + "px";
              last.childNodes.forEach(col => {
                  let coord = Math.floor(col.offsetLeft/this.cellWidth)+"_"+y;
                  this.prepareAskContent(coord, col, ask);
              });
          }
      }

      for (var i = 0; i < Math.abs(changeX); i++) {
          if (changeX > 0) {
              let x = this.prePosX + this.maxX;
              this.$.container.childNodes.forEach(row => {
                  let first = row.firstChild;
                  let last = row.lastChild;
                  row.removeChild(first);
                  row.appendChild(first);
                  first.style.left = (x*this.cellWidth) + "px";
                  let coord = x+"_"+Math.floor(row.offsetTop/this.cellHeight);
                  this.prepareAskContent(coord, first, ask);
              });
              this.prePosX++;

          } else {
              let x = this.prePosX-1;
              this.$.container.childNodes.forEach(row => {
                  let first = row.firstChild;
                  let last = row.lastChild;
                  row.removeChild(last);
                  row.insertBefore(last, first);
                  last.style.left = (x*this.cellWidth) + "px";
                  let coord = x+"_"+Math.floor(row.offsetTop/this.cellHeight);
                  this.prepareAskContent(coord, last, ask);
              });

              this.prePosX--;
          }
      }

      if (ask.length > 0) {
          let that = this;
          setTimeout(() => {that.$server.getContent(ask);}, 1);
      }
  }
}
customElements.define(InfiniteGrid.is, InfiniteGrid);

