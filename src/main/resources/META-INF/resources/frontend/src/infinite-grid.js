import {LitElement, html} from 'lit';

class InfiniteGrid extends LitElement {
    get headerscrollarea() {return this.renderRoot?.querySelector('#headerscrollarea') ?? null;}
    get headers() {return this.renderRoot?.querySelector('#headers') ?? null;}
    get scrollarea() {return this.renderRoot?.querySelector('#scrollarea') ?? null;}
    get container() {return this.renderRoot?.querySelector('#container') ?? null;}
    get storage() {return this.renderRoot?.querySelector('#storage') ?? null;}
  render() {
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
                bottom: 0;
                left: 0;
                right: 0;
                padding: 0;
                margin: 0;
                overflow: auto;
            }

            #headerscrollarea {
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                overflow: hidden;
                z-index: 100;
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

            .frozen > .cell {
                background-color: #f7f7f7;
                z-index: 50;
            }

            .cell.frozen {
                background-color: #f7f7f7;
                z-index: 5;
            }

            .frozen > .cell.frozen {
                z-index: 10;
             }

            .row.frozen > .cell.frozen {
                z-index: 100;
            }




        </style>
   <div id="headerscrollarea">
    <div id="headers" class="container"></div>
   </div>
   <div id="scrollarea" class="scrollarea">
    <div id="container" class="container"></div>
   </div>
   <div id="storage" class="storage"></div>
`;
  }

  connectedCallback() {
      super.connectedCallback();
      var that = this;
      this.requestsInProgress = 0;
      this.requestsAllowed = 2;
      if (this.scrollarea != null) {
        this.scrollarea.addEventListener('scroll', pos => that.ensureFrozen(), {passive: true});
        this.scrollarea.addEventListener('scroll', pos => that.onScroll(), {passive: true});
      }
  }

  static get properties() {
      return {
          dimensions: {type: Object},
          textOnly: {type: Boolean},
          setXYAttributes: {type: Boolean}
      }
  }

  updated(changedProperties) {
    if (changedProperties.get('dimensions')) {
        this.dimensionsChanged(this.dimensions);
    }
  }

  dimensionsChanged(dimensions) {
      this.cellWidth = dimensions.cellWidth;
      this.cellHeight = dimensions.cellHeight;
      this.frozenRows = dimensions.frozenRows;
      this.frozenColumns = dimensions.frozenColumns;

      this.container.style.width = dimensions.cellCountX*this.cellWidth+"px";
      this.container.style.height = dimensions.cellCountY*this.cellHeight+"px";
      this.container.style.marginTop = "-"+(this.frozenRows*this.cellHeight)+"px";

      this.headers.style.width = dimensions.cellCountX*this.cellWidth+"px";
      this.headers.style.height = this.frozenRows*this.cellHeight+"px";
      this.scrollarea.style.marginTop = this.headers.style.height;

      this.refresh();
  }

  createElementFromString(content) {
    var slot = document.createElement('slot');
    slot.innerHTML = content;
	return slot.firstChild;
  }

  refresh() {
      setTimeout(() => this.createGridRowsAndCells(), 100);
  }

  setContent(json) {
     if (this.requestsInProgress > this.requestsAllowed) {
          this.requestsInProgress = 0;
          this.$server.cleanStorage();
          this.redrawGrid();
          return;
      } else {
          this.requestsInProgress--;
      }

      setTimeout(() => {
          JSON.parse(json).forEach(celldata => {
              let cell = this.container.querySelector("#cid"+celldata.x+"_"+celldata.y);
              if (cell === null) {
                  cell = this.headers.querySelector("#cid"+celldata.x+"_"+celldata.y);
              }

              if (cell != null) {
                  if (celldata.m != null) {
                      if (this.setXYAttributes == true) {
                          var template = this.createElementFromString(celldata.m);
                          template.x = celldata.x;
                          template.y = celldata.y;
                          this.clearChildren(cell);
                          cell.appendChild(template);
                      } else if(this.textOnly == true) {
                          cell.innerText = celldata.m;
                      } else {
                          cell.innerHTML = celldata.m;
                      }
                  } else {
                      let c = this.storage.querySelector("#id"+celldata.x+"_"+celldata.y);
                      if (c != null) {
                          this.clearChildren(cell);
                          cell.appendChild(c);
                      }
                  }
              } else {
                  if (celldata.m == null) {
                      this.$server.removeStorageComponentById("id" + celldata.x + "_" + celldata.y);
                  }
              }
          });
      }, 1);
  }

  getMaxX() {
      return Math.ceil(this.scrollarea.offsetWidth/this.cellWidth)+this.bufferX;
  }

  getMaxY() {
      return Math.ceil(this.scrollarea.offsetHeight/this.cellHeight)+this.bufferY;
  }

  createGridRowsAndCells() {
      this.prePosX = 0;
      this.prePosY = 0;
      let ask = [];
      let maxX = this.getMaxX();
      let maxY = this.getMaxY();
      //Clear possible old nodes
      this.clearChildren(this.container);
      this.clearChildren(this.headers);

      for (var y = 0; y < maxY; y++) {
          let row = document.createElement("div");
          row.id = "rid"+y;
          row.y = y;
          row.classList.add("row");
          row.style.top = (y*this.cellHeight)+"px";

          if (y < this.frozenRows) {
              row.classList.add("frozen");
              this.headers.appendChild(row);
          } else {
              this.container.appendChild(row);
          }

          for (var x = 0; x < maxX; x++) {
              let cell = document.createElement('span');
              let coord = x+"_"+row.y;
              cell.id = "cid"+coord;
              cell.x = x;
              cell.y = row.y;
              ask.push(coord);
              cell.classList.add('cell');

              if (x < this.frozenColumns) {
                  cell.classList.add("frozen");
              }

              cell.style.width = this.cellWidth+"px";
              cell.style.height = this.cellHeight+"px";
              row.appendChild(cell);
              cell.style.left = (x*this.cellWidth)+"px";

              if(this.domTemplate != null) {
                  var template = this.createElementFromString(this.domTemplate);
                  template.x = cell.x;
                  template.y = cell.y;
                  cell.appendChild(template);
              }
          }
      }

      if (this.domTemplate == null) {
          this.fetchFromServer(ask);
      }

      this.onScroll();
      this.ensureFrozen();
  }

  ensureNoFrozenX(x) {
      return x + this.frozenColumns;
  }

  calculatePositionsForRowsAndCells(container, initialX, initialY) {
      let ask = [];

      let y = initialY;
      for(let i = 0; i < container.childNodes.length; i++) {
          let row = container.childNodes[i];
          row.id = "rid" + y;
          row.y = y;
          row.style.top = (y * this.cellHeight) + "px";
          y++;

          let x = this.ensureNoFrozenX(initialX);
          for (let j = 0; j < row.childNodes.length; j++) {
              let cell = row.childNodes[j];

              if(!cell.classList.contains("frozen")) {
                  cell.id = "cid" + x + "_" + row.y;
                  cell.x = x;
                  cell.y = row.y;
                  cell.style.left = (cell.x * this.cellWidth) + "px";
                  x++;
              } else {
                  cell.id = "cid"+ cell.x+"_"+row.y;
                  cell.y = row.y;
              }

              this.clearCellAndAskContentFromServer(cell, ask);
          }
      }

      if (this.domTemplate == null) {
          return ask;
      }

      return [];
  }

  clearChildren(node) {
      while(node.firstChild) {
          node.removeChild(node.firstChild);
      }
  }

  clearCellAndAskContentFromServer(cell, ask) {
      if (cell.x < 0 ||
          cell.parentNode.y < 0 ||
          cell.x >= this.dimensions.cellCountX ||
          cell.parentNode.y >= this.dimensions.cellCountY) {
          return;
      }

      if (this.domTemplate == null) {
          for (let i = 0; i < cell.childNodes.length; i++) {
              cell.removeChild(cell.childNodes[i]);
          }
          ask.push(cell.x+"_"+cell.parentNode.y);
      } else {
          for(let i = 0; i < cell.childNodes.length; i++) {
              var child = cell.childNodes[i];
              child.x = cell.x;
              child.y = cell.parentNode.y;
          }
      }
  }

  isRedrawingMoreEfficient(changeX, changeY) {
      return Math.abs(changeY) > (Math.ceil(this.scrollarea.offsetHeight/this.cellHeight)+this.bufferY)/2 ||
          Math.abs(changeX) > (Math.ceil(this.scrollarea.offsetWidth/this.cellWidth)+this.bufferX)/2;
  }

  ensureFrozen() {
      this.headerscrollarea.scrollLeft = this.scrollarea.scrollLeft;
      this.ensureFrozenColumns(this.container);
      this.ensureFrozenColumns(this.headers);
  }

  ensureFrozenColumns(container) {
      for (var j = 0; j < container.childNodes.length; j++) {
          var row = container.childNodes[j];
          for (var i = 0; i < this.frozenColumns; i++) {
              var cell = row.childNodes[i];
              cell.style.left = (container.parentNode.scrollLeft + i*this.cellWidth)+"px";
          }
      }
  }

  getFirst(container) {
      for(var i = 0; i < container.childNodes.length; i++) {
          if (!container.childNodes[i].classList.contains("frozen")) {
              return container.childNodes[i];
          }
      }

      return null;
  }

  getLast(container) {
      for(var i = container.childNodes.length-1; i >= 0; i--) {
          if (!container.childNodes[i].classList.contains("frozen")) {
              return container.childNodes[i];
          }
      }

      return null;
  }

  onScrollYDown(container, ask) {
      let first = container.firstChild;
      let last = container.lastChild;
      container.removeChild(first);
      container.appendChild(first);
      let y = last.y+1;
      if (y >=0 && y < this.frozenRows) {
          y += this.frozenRows;
      }
      this.onScrollY(first, y, ask);
  }

  onScrollYUp(container, ask) {
      let last = container.lastChild;
      let first = container.firstChild;
      container.removeChild(last);
      container.insertBefore(last, first);
      let y = first.y-1;
      if (y >=0 && y < this.frozenRows) {
          y -= this.frozenRows;
      }
      this.onScrollY(last, y, ask);
  }

  onScrollY(row, y, ask) {
      row.style.top = (y*this.cellHeight) + "px";
      row.y = y;
      row.id = "rid"+y;
      for (var i = 0; i < row.childNodes.length; i++) {
          var col = row.childNodes[i];
          let x = Math.floor(col.offsetLeft/this.cellWidth);
          col.id = "cid"+col.x+"_"+y;
          this.clearCellAndAskContentFromServer(col, ask);
      }
  }

  onScrollXDown(container, ask) {
      for (var i = 0; i < container.childNodes.length; i++) {
          var row = container.childNodes[i];
          let first = this.getFirst(row);
          let last = this.getLast(row);
          row.removeChild(first);
          row.appendChild(first);

          let x = last.x+1;
          if (x >= 0 && x < this.frozenColumns) {
              x += this.frozenColumns;
          }

          first.style.left = (x*this.cellWidth) + "px";
          first.x = x;
          first.id = "cid"+x+"_"+first.parentNode.y;
          this.clearCellAndAskContentFromServer(first, ask);
      }
  }

  onScrollXUp(container, ask) {
      for (var i = 0; i < container.childNodes.length; i++) {
          var row = container.childNodes[i];
          let first = this.getFirst(row);
          let last = this.getLast(row);
          row.removeChild(last);
          row.insertBefore(last, first);

          let x = first.x-1;
          if (x >= 0 && x < this.frozenColumns) {
              x -= this.frozenColumns;
          }

          last.style.left = (x*this.cellWidth) + "px";
          last.x = x;
          last.id = "cid"+x+"_"+last.parentNode.y;
          this.clearCellAndAskContentFromServer(last, ask);
      }
  }

  getCurrentX() {
      return Math.floor(this.scrollarea.scrollLeft/this.cellWidth);
  }

  getCurrentY() {
      return Math.floor(this.scrollarea.scrollTop/this.cellHeight);
  }

  onScroll() {
      let currX = this.getCurrentX();
      let currY = this.getCurrentY();
      let changeY = currY - this.prePosY;
      let changeX = currX - this.prePosX;
      this.prePosY = currY;
      this.prePosX = currX;
      var ask = [];
      if (this.isRedrawingMoreEfficient(changeX, changeY) === true) {
          this.redrawGrid();
          return;
      }

      let bufferCutOff = 2;

      let bottom = currY + Math.ceil(this.scrollarea.offsetHeight/this.cellHeight);
      let topVisible = this.container.lastChild.y - this.container.childNodes.length;
      let reserveBottom = this.container.lastChild.y - bottom + 1;
      let reserveTop = currY - topVisible - 1;

      let right = currX + Math.ceil(this.scrollarea.offsetWidth/this.cellWidth);
      let leftVisible = this.container.lastChild.lastChild.x - this.container.lastChild.childNodes.length;
      let reserveRight = this.container.lastChild.lastChild.x - right;
      let reserveLeft = currX - leftVisible - 1;

      if (changeY > 0 && reserveBottom < this.bufferY/bufferCutOff) {
          for (var i = 0; i <  Math.abs(reserveTop-1); i++) {
              this.onScrollYDown(this.container, ask);
          }
      } else if (changeY < 0 && reserveTop < this.bufferY/bufferCutOff) {
          for (var i = 0; i <  Math.abs(reserveBottom-1); i++) {
              this.onScrollYUp(this.container, ask);
          }
      }

      if (changeX > 0 && reserveRight < this.bufferX/bufferCutOff) {
          for (var i = 0; i <  Math.abs(reserveLeft-1); i++) {
              this.onScrollXDown(this.container, ask);
              this.onScrollXDown(this.headers, ask);
          }
      } else if (changeX < 0 && reserveLeft < this.bufferX/bufferCutOff) {
          for (var i = 0; i <  Math.abs(reserveRight-1); i++) {
              this.onScrollXUp(this.container, ask);
              this.onScrollXUp(this.headers, ask);
          }
      }

      this.fetchFromServer(ask);
  }

  redrawGrid() {
      let ask = this.calculatePositionsForRowsAndCells(this.container, this.getCurrentX(), this.getCurrentY()+this.frozenRows)
          .concat(this.calculatePositionsForRowsAndCells(this.headers, this.getCurrentX(), 0));
      this.fetchFromServer(ask);
  }

  fetchFromServer(ask) {
      if(ask.length > 0) {
          if (this.requestsInProgress <= this.requestsAllowed) {
              this.$server.getContent(ask);
          }

          this.requestsInProgress++;
      }
  }
}

customElements.define('infinite-grid', InfiniteGrid);

