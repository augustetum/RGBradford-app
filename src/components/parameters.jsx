import React from 'react';

const Parameters = ({
  coords,
  setCoordsOrigin,
  setCoordsEnd,
  toggleSelectMode,
  setSizeMode,
  selectMode,
  crop,
  selection,
  reset,
  sizeMode,
  setSelectMode,
  setIsDrawing,
  setSizeLine,
  handleSubmit,
  columnRef,
  rowRef,
  coordsOrigin,
  coordsEnd,
  measuredDistance,
  setRowCount,
  setColumnCount,
}) => {
  return (
    <div className="w-[min(90vw,50rem)]">
      <div className="flex gap-4 justify-center mt-6 flex-wrap">
        <button onClick={() => {setCoordsOrigin({ ...coords });}} className="btn">
          Set Top Left Well Coordinates
        </button>
        <button onClick={() => {setCoordsEnd({ ...coords });}} className="btn">
          Set Bottom Right Well Coordinates
        </button>
        <button
          onClick={() => {toggleSelectMode(); setSizeMode(false);}}
          className={`btn ${
            selectMode ? "!bg-red-600 !text-white" : ""
          }`}
        >
          {selectMode ? "Exit Zoom Mode" : "Enter Zoom Mode"}
        </button>
        <button
          onClick={crop}
          disabled={!selection || selection.w === 0 || selection.h === 0}
          className="btn"
        >
          Zoom
        </button>
        <button
          onClick={reset}
          className="btn"
        >
          Reset Zoom
        </button>
        <button
          onClick={() => {
            setSizeMode((m) => !m);
            setSelectMode(false); // turn off rectangle select when size mode is on
            setIsDrawing(false);
            setSizeLine(null);
          }}
          className={`btn ${sizeMode ? "!bg-red-600 !text-white" : ""}`}
        >
          {sizeMode ? "Exit Diameter Select Mode" : "Enter Diameter Select Mode"}
        </button>
        <button onClick={handleSubmit} className="btn !bg-green-500 font-bold">
          Submit
        </button>
      </div>
      <form className="mt-6 flex gap-4 text-xl justify-center flex-wrap" action="">
        <span>
          <p className="!text-white">Columns</p>
          <input className="inpt" type="number" name="columns" ref={columnRef} id="columns" defaultValue="12" onChange={(e) => setColumnCount(e.target.value)}/>
        </span>
        <span>
          <p className="!text-white">Rows</p>
          <input className="inpt" type="number" name="rows" ref={rowRef} id="rows" defaultValue="8" onChange={(e) => setRowCount(e.target.value)}/>
        </span>
        <span>
          <p className="!text-white">xOrigin</p>
          <input className="inpt" readOnly={true} name="xOrigin" id="xOrigin" value={coordsOrigin ? coordsOrigin.x : ""} />
        </span>
        <span>
          <p className="!text-white">yOrigin</p>
          <input className="inpt" readOnly={true} name="yOrigin" id="yOrigin" value={coordsOrigin ? coordsOrigin.y : ""} />
        </span>
        <span>
          <p className="!text-white">xEnd</p>
          <input className="inpt" readOnly={true} name="xEnd" id="xEnd" value={coordsEnd ? coordsEnd.x : ""} />
        </span>
        <span>
          <p className="!text-white">yEnd</p>
          <input className="inpt" readOnly={true} name="yEnd" id="yEnd" value={coordsEnd ? coordsEnd.y : ""} />
        </span>
        <span>
          <p className="!text-white">wellDiameter</p>
          <input className="inpt" readOnly={true} name="wellDiameter" id="wellDiameter" value={measuredDistance ? Math.round(measuredDistance/2) : ""} />
        </span>
      </form>
    </div>
  );
};

export default Parameters;