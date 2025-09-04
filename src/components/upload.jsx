import React, { useState, useRef, useEffect, useCallback, use  } from "react";
import plusIcon from "../assets/plus.svg"
import { AnimatePresence, motion } from 'framer-motion';

function Upload() {  
  const [image, setImage] = useState(null);
  const [coords, setCoords] = useState({x: 0, y:0});
  const [coordsOrigin, setCoordsOrigin] = useState(null);
  const [coordsEnd, setCoordsEnd] = useState(null);
  const originalImgRef = useRef(null);
  const fileInputRef = useRef(null);
  const [originalImage, setOriginalImage] = useState(image);
  const [displayedImage, setDisplayedImage] = useState(originalImage);
  const [selectMode, setSelectMode] = useState(false);
  const [selection, setSelection] = useState(); // { x, y, w, h }
  const [selectionOld, setOldSelection] = useState(null);
  const [isDrawing, setIsDrawing] = useState(false);
  const canvasRef = useRef(null)
  const [sizeMode, setSizeMode] = useState(false);
  const [sizeLine, setSizeLine] = useState(null); // { x1, y1, x2, y2 }
  const [measuredDistance, setMeasuredDistance] = useState(null);
  const [uploadStage, setUploadStage] = useState('parameters');

  const handleInputContainerClick = () => {
    fileInputRef.current.click();
  };

  const handleUpload = (e) => {
    const file = e.target.files[0];
    if (file && file.type.startsWith("image/")) {
      const url = URL.createObjectURL(file);
      setOriginalImage(url);
      setImage(url);
      setDisplayedImage(url);
      const img = new Image();
      img.onload = () => {
        originalImgRef.current = img;
      };
      img.src = url;
    }
  };

  const handleClick = (e) => {
    const canvas = canvasRef.current;
    const img = originalImgRef.current
    if (!canvas) {console.log("no canvas"); return;}
    const rect = canvas.getBoundingClientRect();
    
    const x = e.clientX - rect.left;
    const y = e.clientY - rect.top;
    
    const scaleX = img.naturalWidth / rect.width;
    const scaleY = img.naturalHeight / rect.height;
    
    let pixelX = 0
    let pixelY = 0
    if (selectionOld){
      pixelX = Math.round(x * scaleX * selectionOld.w / img.naturalWidth + selectionOld.x);
      pixelY = Math.round(y * scaleY * selectionOld.h / img.naturalHeight + selectionOld.y);
    }
    if (!selectionOld) {
      pixelX = Math.round(x * scaleX);
      pixelY = Math.round(y * scaleY);
    }
    setCoords({ x: pixelX, y: pixelY });
  };
  
    const loadImage = useCallback((src) => {
      return new Promise((resolve, reject) => {
        const img = new Image();
        img.crossOrigin = "anonymous";
        img.onload = () => resolve(img);
        img.onerror = reject;
        img.src = src;
      });
    }, []);
    

    

    const draw = useCallback(async () => {
      const canvas = canvasRef.current;
      console.log("called draw")
      if (!canvas) return;
      
      const ctx = canvas.getContext("2d");
      
      const img = await loadImage(displayedImage);
      const width = img.naturalWidth || img.width;
      const height = img.naturalHeight || img.height;
    
      canvas.width = width;
      canvas.height = height;
    
      ctx.clearRect(0, 0, width, height);
      ctx.drawImage(img, 0, 0, width, height);
      if (selection) {
        ctx.save();
        ctx.fillStyle = "rgba(0,0,0,0.35)";
        ctx.beginPath();
        ctx.rect(0, 0, width, height);
        ctx.rect(selection.x, selection.y, selection.w, selection.h);
        ctx.fill("evenodd");
    
        ctx.strokeStyle = "red";
        ctx.lineWidth = 2;
        ctx.setLineDash([6, 4]);
        ctx.strokeRect(selection.x, selection.y, selection.w, selection.h);
        ctx.restore();
      }
    
      if (coordsOrigin) {
        ctx.beginPath();
        ctx.fillStyle = "green";
        if (!selectionOld) ctx.arc(coordsOrigin.x, coordsOrigin.y, 5, 0, Math.PI * 2);
        else ctx.arc(coordsOrigin.x-selectionOld.x, coordsOrigin.y-selectionOld.y, 5, 0, Math.PI * 2);
        ctx.fill();
      }
      if (coordsEnd) {
        ctx.beginPath();
        ctx.fillStyle = "red";
        if (!selectionOld) ctx.arc(coordsEnd.x, coordsEnd.y, 5, 0, Math.PI * 2);
        else ctx.arc(coordsEnd.x-selectionOld.x, coordsEnd.y-selectionOld.y, 5, 0, Math.PI * 2);
        ctx.fill();
      }
      if (sizeLine) {
        ctx.beginPath();
        ctx.strokeStyle = "red";
        ctx.lineWidth = 2;
        ctx.setLineDash([]); 
        if (!selectionOld || sizeMode) {
          ctx.moveTo(sizeLine.x1, sizeLine.y1);
          ctx.lineTo(sizeLine.x2, sizeLine.y2);
        } else {
          ctx.moveTo(sizeLine.x1 - selectionOld.x, sizeLine.y1 - selectionOld.y);
          ctx.lineTo(sizeLine.x2 - selectionOld.x, sizeLine.y2 - selectionOld.y);
        }
        ctx.stroke();
      }
      for (let indexColumns = 0; indexColumns < array.length; indeColumnsx++) {
        for (let indexRows = 0; indexRows < array.length; indexRows++) {
          const element = array[index];
          
        }
        const element = array[index];
        
      }
    }, [displayedImage, selection, coordsOrigin, coordsEnd, loadImage, sizeLine]);
    
    useEffect(() => {
      if (displayedImage) {
        draw();
      }
    }, [draw, displayedImage, selection, coordsOrigin, coordsEnd, sizeLine]);
    
  
    const getMousePos = (e) => {
      const canvas = canvasRef.current;
      const rect = canvas.getBoundingClientRect();
      const scaleX = canvas.width / rect.width;
      const scaleY = canvas.height / rect.height;
      const x = (e.clientX - rect.left) * scaleX;
      const y = (e.clientY - rect.top) * scaleY;
      return {
        x: Math.max(0, Math.min(canvas.width, x)),
        y: Math.max(0, Math.min(canvas.height, y)),
      };
    };
  
    const onMouseDown = (e) => {
      if (selectMode){
      setIsDrawing(true);
      const { x, y } = getMousePos(e);
      setSelection({ x, y, w: 0, h: 0 });
      }
      else if (sizeMode) {
      setIsDrawing(true);
      const { x, y } = getMousePos(e);
      setSizeLine({ x1: x, y1: y, x2: x, y2: y });
      }
    };
  
    const onMouseMove = (e) => {
      if (!isDrawing) return;
      if (selectMode && selection) {
      const { x, y } = getMousePos(e);
      const x0 = Math.min(x, selection.x);
      const y0 = Math.min(y, selection.y);
      const w = Math.abs(x - selection.x);
      const h = Math.abs(y - selection.y);
      setSelection({ x: x0, y: y0, w, h });
      }
      else if (sizeMode && sizeLine) {
        const { x, y } = getMousePos(e);
        setSizeLine((prev) => ({ ...prev, x2: x, y2: y }));
  }
    };
  
    const onMouseUp = () => {
      if (sizeMode && sizeLine) {
        const dx = sizeLine.x2 - sizeLine.x1;
        const dy = sizeLine.y2 - sizeLine.y1;
        const dist = Math.sqrt(dx * dx + dy * dy);
        setMeasuredDistance(dist);
      }
      setIsDrawing(false);
      };
    const onMouseLeave = () => setIsDrawing(false);
  
    const crop = async () => {
      if (!selection || selection.w === 0 || selection.h === 0) return;
      const img = await loadImage(displayedImage);
      const off = document.createElement("canvas");
      const w = Math.round(selection.w);
      const h = Math.round(selection.h);
      off.width = w;
      off.height = h;
      const octx = off.getContext("2d");
      octx.imageSmoothingEnabled = false;
      octx.drawImage(
        img,
        Math.round(selection.x),
        Math.round(selection.y),
        w,
        h,
        0,
        0,
        w,
        h
      );
      const dataUrl = off.toDataURL("image/png");
      setDisplayedImage(dataUrl);
      if (!selectionOld) setOldSelection({x: selection.x, y: selection.y, w: selection.w, h: selection.h});
      else setOldSelection({x: selection.x+selectionOld.x, y: selection.y+selectionOld.y, w: selection.w, h: selection.h})
      setSelection(null);
    };
  
    const reset = () => {
      setSizeMode(false)
      setDisplayedImage(originalImage);
      setOldSelection(null);
      setSelection(null);
    };
  
    const toggleSelectMode = () => {
      setSelectMode((s) => !s);
      setIsDrawing(false);
      setSelection(null);
    };

    const handleSubmit = () => {
      setUploadStage('calibration');
      reset();
    }

  return (
    <>
          <AnimatePresence mode="wait">
      <motion.div
       key={image}
       initial={{ opacity: 0, y: 20 }}
       animate={{ opacity: 1, y: 0 }}
       exit={{ opacity: 0, y: -20 }}
       transition={{ duration: 0.2 }}
       onAnimationComplete={() => {
        if (canvasRef.current && displayedImage) {
          draw();
        }
      }}
      >
        {!image &&(<button onClick={handleInputContainerClick} className='cursor-pointer w-[min(80vw,50rem)] h-[30vh] bg-igem-gray 
          rounded-xl flex justify-center items-center'>
        <div>
          <img src={plusIcon} className='mx-auto w-10 ' />
          <input
          type="file"
          accept="image/*"
          onChange={handleUpload}
          ref={fileInputRef}
          className="hidden text-center"
          />
          <p>upload plate</p>
        </div>
      </button>)}

      
        <div className={`${image ? "" : "hidden"} my-6`}>
        <div className=" w-[min(80vw,50rem)] p-4 bg-igem-gray 
          rounded-xl flex flex-col">
          
        <canvas
          ref={canvasRef}
          onMouseDown={onMouseDown}
          onMouseMove={onMouseMove}
          onMouseUp={onMouseUp}
          onMouseLeave={onMouseLeave}
          className="border rounded-2xl shadow w-full h-auto"
          onClick={handleClick}
        />
          
          <p className={`${uploadStage == 'parameters' ? "" : "hidden"} mt-4 !text-igem-black  text-lg font-mono`}>
            Clicked at: ({coords.x}, {coords.y}, selection: {selectionOld ? selectionOld.x : ""}, {selectionOld ? selectionOld.y : ""}, {selectionOld ? selectionOld.w : ""}, {selectionOld ? selectionOld.h : ""})
          </p>
        

        </div>
        {uploadStage == 'parameters' && (
        <div className="w-[min(80vw,50rem)]">
          <div className="flex gap-4 justify-center mt-6 flex-wrap">
            <button onClick={() => setImage(null)} className="btn">
              Clear Image
            </button>
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
            <span ><p className="!text-white">Columns</p><input className="inpt" type="number" name="columns" id="columns" defaultValue="12" /></span>
            <span><p className="!text-white">Rows</p><input className="inpt" type="number" name="rows" id="rows" defaultValue="8" /></span>
            <span><p className="!text-white">xOrigin</p><input className="inpt" readOnly={true} type="number" name="xOrigin" id="xOrigin" value={coordsOrigin ? coordsOrigin.x : ""} /></span>
            <span><p className="!text-white">yOrigin</p><input className="inpt" readOnly={true} type="number" name="yOrigin" id="yOrigin" value={coordsOrigin ? coordsOrigin.y : ""} /></span>
            <span><p className="!text-white">xEnd</p><input className="inpt" readOnly={true} type="number" name="xEnd" id="xEnd" value={coordsEnd ? coordsEnd.x : ""} /></span>
            <span><p className="!text-white">yEnd</p><input className="inpt" readOnly={true} type="number" name="yEnd" id="yEnd" value={coordsEnd ? coordsEnd.y : ""} /></span>
            <span><p className="!text-white">wellDiameter</p><input className="inpt" readOnly={true} type="number" name="wellDiameter" id="wellDiameter" value={measuredDistance ? Math.round(measuredDistance/2) : ""} /></span>
          </form>
        </div>)}
    </div>
    </motion.div>
    </AnimatePresence>
    </>
  );
}

export default Upload