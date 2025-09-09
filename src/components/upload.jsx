import React, { useState, useRef, useEffect, useCallback, use  } from "react";
import plusIcon from "../assets/plus.svg"
import { AnimatePresence, motion } from 'framer-motion';
import WellSelection from "./wellSelection.jsx";
import Calibration from "./calibration.jsx";
import Crop from "./crop.jsx";
import Parameters from "./parameters.jsx";
import tutorial from "../tutorial.jsx";

function Upload() {  
  const [image, setImage] = useState(null);
  const [coords, setCoords] = useState({x: 0, y:0});
  const [coordsOrigin, setCoordsOrigin] = useState(null);
  const [coordsEnd, setCoordsEnd] = useState(null);
  const originalImgRef = useRef(null);
  const fileInputRef = useRef(null);
  const rowRef = useRef(null);
  const columnRef = useRef(null);
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
  const [uploadStage, setUploadStage] = useState('upload');
  const [wellCenters, setWellCenters] = useState([]);
  const [name, setName] = useState();
  const [description, setDescription] = useState();
  const [projectId, setProjectId] = useState(); 
  const [rowCount, setRowCount] = useState(8); 
  const [columnCount, setColumnCount] = useState(12); 
  const [plateId, setPlateId] = useState();
  
  const getFileExtension = (mimeType) => {
    const extensions = {
      'image/jpeg': 'jpg',
      'image/png': 'png',
      'image/heic': 'heic',
    };
    return extensions[mimeType] || 'jpg';
  };


  const handleDescriptionName = async (e, name, description) => {
    e.preventDefault();
    const token = localStorage.getItem("token");
    try {
      const response = await fetch("https://rgbradford-app.onrender.com/api/projects", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ name, description }),
      });
      const data = await response.json();
      if (response.ok) {
        setProjectId(data.id)
        alert("Project creation successful!", data.id);
      } else {
        alert(data.message || "Project creation failed");
      }
    } catch (error) {
      console.error("Error:", error);
      alert("⚠️ Server error. Try again later.");
    }
  };

  const handleFinalSubmit = async () => {
    const token = localStorage.getItem("token");
    console.log("id", projectId)
    try {
      const response = await fetch("https://rgbradford-app.onrender.com/api/plate-layouts", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          "Authorization": `Bearer ${token}`,
        },
        body: JSON.stringify({ 
          row: rowCount, 
          column: columnCount, 
          projectId: projectId, 
        }),
      });
      const data = await response.json();
      if (response.ok) {
        const currentPlateId = data.id
        setPlateId(currentPlateId)
        alert("R/C and id submit successful!");
        const refactoredWells = wellCenters.map(obj => ({
          row: obj.indexRow, 
          column: obj.indexColumn,
          type: obj.type,
          standardConcentration: obj.standardConcentration,
          replicateGroup: obj.replicateGroup, 
        }))
        try {
          console.log(refactoredWells.length)
          const response2 = await fetch(`https://rgbradford-app.onrender.com/api/plate-layouts/${currentPlateId}/wells`, {
            method: "POST", 
            headers: {
              "Content-Type": "application/json",
              "Authorization": `Bearer ${token}`,
            },
            body: JSON.stringify(refactoredWells),
          })
          if (response2.ok) {
            alert("all good chief")
            const extension = getFileExtension(displayedImage.type);
            const file = new File([displayedImage], `image.${extension}`, {
              type: displayedImage.type,
              lastModified: Date.now()
            });
            const formData = new FormData();
            formData.append('image', file);
            formData.append("plateLayoutId", parseInt(currentPlateId));
            formData.append("params", JSON.stringify({
              columns: columnCount,
              rows: rowCount,
              xorigin: coordsOrigin.x,
              yorigin: coordsOrigin.y,
              xend: coordsEnd.x,
              yend: coordsEnd.y,
              wellDiameter: measuredDistance,
            })); 
            try {
              const response2 = await fetch(`https://rgbradford-app.onrender.com/api/plate-analysis/analyze`, {
                method: "POST", 
                headers: {
                  "Authorization": `Bearer ${token}`,
                },
                body: formData,
              })
              if (response2.ok) {
                alert("all good chief x2")
              }
              else {
                alert("failed 3rd req")
              }
            } catch (error) {
              console.error("Error:", error);
              alert("⚠️ req 2 error. Try again later.");
            }
          }
          else {
            alert("failed 2nd req")
          }
        } catch (error) {
          console.error("Error:", error);
          alert("⚠️ req 2 error. Try again later.");
        }
        
      } else {
        alert(data.message || "failed");
      }
    } catch (error) {
      console.error("Error:", error);
      alert("⚠️ req 1 error. Try again later.");
    }
  };




  const handleUpload = (e) => {
    const file = e.target.files[0];
    if (file && file.type.startsWith("image/")) {
      const url = URL.createObjectURL(file);
      setOriginalImage(url);
      setImage(url);
      setDisplayedImage(url);
      setUploadStage('crop');
      const img = new Image();
      img.onload = () => {
        originalImgRef.current = img;
      };
      img.src = url;
      handleDescriptionName(e, name, description)
    }
  };

  const setRefFromUrl = (url) => {
    const img = new Image();
    img.onload = () => {
      originalImgRef.current = img
      ;};
    img.src = url;
  };

  const handleClick = (e, ref) => {

    const canvas = ref.current;
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
    return { x: pixelX, y: pixelY };
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

      if (coordsEnd && coordsOrigin && measuredDistance) {
        const offsetX = selectionOld ? selectionOld.x : 0
        const offsetY = selectionOld ? selectionOld.y : 0
        const diameter = measuredDistance
        const gapX = (Math.abs(coordsOrigin.x-coordsEnd.x)-(columnCount-1)*diameter)/(columnCount-1) 
        const gapY = (Math.abs(coordsOrigin.y-coordsEnd.y)-(rowCount-1)*diameter)/(rowCount-1) 
        
        for (let indexColumn = 0; indexColumn < columnCount; indexColumn++) {
          for (let indexRow = 0; indexRow < rowCount; indexRow++) {
            let coordX = coordsOrigin.x + diameter*indexColumn + gapX*indexColumn - offsetX
            let coordY = coordsOrigin.y + diameter*indexRow + gapY*indexRow - offsetY
            ctx.beginPath();
            ctx.arc(coordX, coordY,
                    diameter/2,0,Math.PI*2)
            ctx.stroke()
            
          }
        }
      }
    }, [displayedImage, selection, coordsOrigin, coordsEnd, loadImage, sizeLine]);
    
    useEffect(() => {
      if (displayedImage) {
        draw();
      }
    }, [draw, displayedImage, selection, coordsOrigin, coordsEnd, sizeLine]);
    
  
    const getPointerPos = (e, ref) => {
      const canvas = ref.current;
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
  
    const onPointerDown = (e) => {
      e.preventDefault();
      if (selectMode){
      setIsDrawing(true);
      const { x, y } = getPointerPos(e, canvasRef);
      setSelection({ x, y, w: 0, h: 0 });
      }
      else if (sizeMode) {
      setIsDrawing(true);
      const { x, y } = getPointerPos(e, canvasRef);
      setSizeLine({ x1: x, y1: y, x2: x, y2: y });
      }
    };
  
    const onPointerMove = (e) => {
      if (!isDrawing) return;
      if (selectMode && selection) {
      const { x, y } = getPointerPos(e, canvasRef);
      const x0 = Math.min(x, selection.x);
      const y0 = Math.min(y, selection.y);
      const w = Math.abs(x - selection.x);
      const h = Math.abs(y - selection.y);
      setSelection({ x: x0, y: y0, w, h });
      }
      else if (sizeMode && sizeLine) {
        const { x, y } = getPointerPos(e, canvasRef);
        setSizeLine((prev) => ({ ...prev, x2: x, y2: y }));
    }
    };
  
    const onPointerUp = () => {
      if (sizeMode && sizeLine) {
        const dx = sizeLine.x2 - sizeLine.x1;
        const dy = sizeLine.y2 - sizeLine.y1;
        const dist = Math.sqrt(dx * dx + dy * dy);
        setMeasuredDistance(dist);
      }
      setIsDrawing(false);
      };
    const onPointerLeave = () => setIsDrawing(false);
  
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
      if (coordsEnd && coordsOrigin && measuredDistance) {
        const offsetX = 0
        const offsetY = 0
        const diameter = measuredDistance
        const gapX = (Math.abs(coordsOrigin.x-coordsEnd.x)-(columnCount-1)*diameter)/(columnCount-1) 
        const gapY = (Math.abs(coordsOrigin.y-coordsEnd.y)-(rowCount-1)*diameter)/(rowCount-1) 
        for (let indexColumn = 0; indexColumn < columnCount; indexColumn++) {
          for (let indexRow = 0; indexRow < rowCount; indexRow++) {
            let coordX = coordsOrigin.x + diameter*indexColumn + gapX*indexColumn - offsetX
            let coordY = coordsOrigin.y + diameter*indexRow + gapY*indexRow - offsetY     
            setWellCenters(prev => ([...prev, {'x':coordX, 'y':coordY, 'indexColumn' : indexColumn, 'indexRow' : indexRow}]))
          }
        }
        reset()
        setUploadStage('wellSelection');
      }
    }
    
    const handleInputContainerClick = (e) => {
      fileInputRef.current.click();
    };

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
      {tutorial[uploadStage]}

      {uploadStage === 'upload' && (
        <>
        <button onClick={handleInputContainerClick} className='cursor-pointer w-[min(90vw,50rem)] h-[30vh] bg-igem-gray 
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
      </button>
      <form action="" className="flex gap-10 justify-center mt-4">
        <span>name<input type="text" className="inpt" onChange={(e) => setName(e.target.value)}/></span>
        <span>desc<input type="text" className="inpt" onChange={(e) => setDescription(e.target.value)}/></span>
      </form>
      </>
    )}

      {image && (uploadStage === 'parameters' || uploadStage === 'crop') && (
        <div className="mb-6">
          <div className="p-2 bg-igem-gray rounded-xl flex flex-col">
            <canvas
              ref={canvasRef}
              onPointerDown={onPointerDown}
              onPointerMove={onPointerMove}
              onPointerUp={onPointerUp}
              onPointerLeave={onPointerLeave}
              className="border rounded-2xl shadow w-auto h-full touch-none"
              onClick={(e) => handleClick(e, canvasRef)}
            />
            {uploadStage === 'parameters' && (
              <p className="mt-4 !text-igem-black text-lg font-mono">
                Clicked at: ({coords.x}, {coords.y})
              </p>
            )}
          </div>
        </div>
      )}
        
      {(uploadStage === 'crop') && (
      <Crop 
        setImage={setImage}
        toggleSelectMode={toggleSelectMode}
        setSizeMode={setSizeMode}
        selectMode={selectMode}
        crop={crop}
        selection={selection}
        reset={reset}
        setRefFromUrl={setRefFromUrl}
        displayedImage={displayedImage}
        setUploadStage={setUploadStage}
        setSelectMode={setSelectMode}
        setOriginalImage={setOriginalImage}
        setOldSelection={setOldSelection}
      />
    )}

      {uploadStage === 'parameters' && (
        <Parameters
          coords={coords}
          setCoordsOrigin={setCoordsOrigin}
          setCoordsEnd={setCoordsEnd}
          toggleSelectMode={toggleSelectMode}
          setSizeMode={setSizeMode}
          selectMode={selectMode}
          crop={crop}
          selection={selection}
          reset={reset}
          sizeMode={sizeMode}
          setSelectMode={setSelectMode}
          setIsDrawing={setIsDrawing}
          setSizeLine={setSizeLine}
          handleSubmit={handleSubmit}
          columnRef={columnRef}
          rowRef={rowRef}
          coordsOrigin={coordsOrigin}
          coordsEnd={coordsEnd}
          measuredDistance={measuredDistance}
          setColumnCount={setColumnCount}
          setRowCount={setRowCount}
        />
      )}


      {uploadStage == 'wellSelection' && (
      <WellSelection 
        originalImage={displayedImage}
        setUploadStage={setUploadStage}
        setWellCenters={setWellCenters}
        getPointerPos={getPointerPos}
        measuredDistance={measuredDistance}
        loadImage={loadImage}
        wellCenters={wellCenters}
      />
      )}

      {uploadStage == 'calibration' && (
      <Calibration 
        handleFinalSubmit={handleFinalSubmit}
        setUploadStage={setUploadStage}
        setWellCenters={setWellCenters}
        wellCenters={wellCenters}
      />
      )}
    </motion.div>
    </AnimatePresence>
    </>
  );
}

export default Upload