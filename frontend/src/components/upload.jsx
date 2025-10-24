import React, { useState, useRef, useEffect, useCallback, use  } from "react";
import plusIcon from "../assets/plus.svg"
import { AnimatePresence, motion } from 'framer-motion';
import WellSelection from "./wellSelection.jsx";
import Calibration from "./calibration.jsx";
import Crop from "./crop.jsx";
import Parameters from "./parameters.jsx";
import tutorial from "../tutorial.jsx";
import { desc } from "motion/react-client";
import { API_BASE_URL } from "../config";

function Upload({setCurrentScreen, showNotification, showLoading, hideLoading}) {  
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
  const cvDataRef = useRef(null); // Store CV values for each well
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
  const [showQualityCheck, setShowQualityCheck] = useState(true);
  const [hoveredWell, setHoveredWell] = useState(null); // { row, col, cv }

  const getFileExtension = (mimeType) => {
    const extensions = {
      'image/jpeg': 'jpg',
      'image/png': 'png',
      'image/heic': 'heic',
    };
    return extensions[mimeType] || 'jpg';
  };


  const createProject = async (name, description, token) => {
  const response = await fetch(`${API_BASE_URL}/projects`, {
    method: "POST",
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify({ name, description }),
  });
  
  const data = await response.json();
  if (!response.ok) {
    throw new Error(data.message || "Project creation failed");
  }
  
  return data.id;
};

const createPlateLayout = async (projectId, rowCount, columnCount, token) => {
  const response = await fetch(`${API_BASE_URL}/plate-layouts`, {
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
  if (!response.ok) {
    throw new Error(data.message || "Plate layout creation failed");
  }
  
  return data.id;
};

const submitWells = async (plateId, wellCenters, token) => {
  const refactoredWells = wellCenters.map(obj => ({
    row: obj.indexRow, 
    column: obj.indexColumn,
    type: obj.type,
    standardConcentration: obj.standardConcentration,
    replicateGroup: obj.replicateGroup, 
  }));
  console.log(refactoredWells)
  console.log(wellCenters)
  const response = await fetch(`${API_BASE_URL}/plate-layouts/${plateId}/wells`, {
    method: "POST", 
    headers: {
      "Content-Type": "application/json",
      "Authorization": `Bearer ${token}`,
    },
    body: JSON.stringify(refactoredWells),
  });
  
  if (!response.ok) {
    throw new Error("Wells submission failed");
  }
};


const uploadAndAnalyzeImage = async (plateId, displayedImage, analysisParams, token) => {
  const extension = getFileExtension(displayedImage.type);
  const imageResponse = await fetch(displayedImage);
  const blob = await imageResponse.blob();
  const file = new File([blob], `image.${extension}`, {
    type: displayedImage.type,
    lastModified: Date.now()
  });
  console.log(file)
  const formData = new FormData();
  formData.append("plateLayoutId", parseInt(plateId));
  formData.append("params", JSON.stringify(analysisParams)); 
  formData.append('image', file);

  const response = await fetch(`${API_BASE_URL}/plate-analysis/analyze`, {
    method: "POST", 
    headers: {
      "Authorization": `Bearer ${token}`,
    },
    body: formData,
  });
  
  if (!response.ok) {
    throw new Error("Image analysis failed");
  }
};

const handleError = (error) => {
  console.error("Error:", error);
  showNotification(`⚠️ ${error.message || "An error occurred. Try again later."}`);
};

const showSuccess = (message) => {
  showNotification(message, 'success');
};

const handleDescriptionName = async (e, name, description) => {
  e.preventDefault();
  const token = localStorage.getItem("token");
  
  try {
    const projectId = await createProject(name, description, token);
    setProjectId(projectId);
    showSuccess("Project creation successful!");
  } catch (error) {
    handleError(error);
  }
};

const handleFinalSubmit = async (updatedWellCenters) => {
  const token = localStorage.getItem("token");

  const analysisParams = {
    columns: columnCount,
    rows: rowCount,
    xorigin: coordsOrigin.x,
    yorigin: coordsOrigin.y,
    xend: coordsEnd.x,
    yend: coordsEnd.y,
    wellDiameter: measuredDistance,
  };

  try {
    showLoading("Creating plate layout...");
    const plateId = await createPlateLayout(projectId, rowCount, columnCount, token);
    setPlateId(plateId);

    showLoading("Submitting well data...");
    await submitWells(plateId, updatedWellCenters, token);

    showLoading("Uploading and analyzing image...");
    await uploadAndAnalyzeImage(plateId, displayedImage, analysisParams, token);

    showLoading("Generating calibration curve...");
    await handleCalibrationGet(plateId, token);

    hideLoading("✅ All operations completed successfully!", "success");
    setCurrentScreen('catalog');

  } catch (error) {
    hideLoading();
    handleError(error);
  }
};

  const handleCalibrationGet = async (plateId, token) => {
    const response = await fetch(`${API_BASE_URL}/standard-curve/${plateId}`, {
    method: "GET", 
    headers: {
      "Authorization": `Bearer ${token}`,
    },
  }); 
    if (!response.ok) {
      throw new Error("Image analysis failed");
    }
    const data = await response.json();
    console.log(data);
  }


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

    // Helper function to get pixels within a circular region
    const getCirclePixels = (imageData, centerX, centerY, radius) => {
      const pixels = [];
      const minX = Math.floor(centerX - radius);
      const maxX = Math.ceil(centerX + radius);
      const minY = Math.floor(centerY - radius);
      const maxY = Math.ceil(centerY + radius);
      const radiusSquared = radius * radius;

      for (let y = minY; y <= maxY; y++) {
        for (let x = minX; x <= maxX; x++) {
          const dx = x - centerX;
          const dy = y - centerY;
          const distSquared = dx * dx + dy * dy;

          if (distSquared <= radiusSquared) {
            const index = (y * imageData.width + x) * 4;
            if (index >= 0 && index < imageData.data.length) {
              const r = imageData.data[index];
              const g = imageData.data[index + 1];
              const b = imageData.data[index + 2];
              pixels.push({ r, g, b });
            }
          }
        }
      }
      return pixels;
    };

    // Calculate coefficient of variation for green/blue ratio
    const calculateGreenBlueCV = (pixels) => {
      const ratios = [];

      for (const pixel of pixels) {
        if (pixel.b > 0) { // Exclude pixels where blue = 0
          ratios.push(pixel.g / pixel.b);
        }
      }

      if (ratios.length === 0) return null;

      // Calculate mean
      const mean = ratios.reduce((sum, val) => sum + val, 0) / ratios.length;

      // Calculate standard deviation
      const variance = ratios.reduce((sum, val) => sum + Math.pow(val - mean, 2), 0) / ratios.length;
      const stdDev = Math.sqrt(variance);

      // Calculate CV
      const cv = mean !== 0 ? stdDev / mean : 0;

      return cv;
    };

    // Map CV value to color using continuous HSL gradient
    // Green (120° hue) at CV=0.0 -> Yellow (60°) at CV=0.125 -> Red (0°) at CV>=0.25
    const getCVColor = (cv) => {
      if (cv === null) return 'rgba(128, 128, 128, 0.5)'; // Gray for no data

      // Clamp CV to max of 0.25 for color scale
      const clampedCV = Math.min(cv, 0.25);

      // Calculate hue: 120 (green) to 0 (red) based on CV
      // At CV=0.0: hue=120 (green)
      // At CV=0.125: hue=60 (yellow)
      // At CV=0.25: hue=0 (red)
      const hue = Math.max(0, 120 - (clampedCV / 0.25) * 120);

      return `hsla(${hue}, 100%, 50%, 0.5)`;
    };

    const draw = useCallback(async () => {
      const canvas = canvasRef.current;
      if (!canvas) return;

      const ctx = canvas.getContext("2d");

      const img = await loadImage(displayedImage);
      const width = img.naturalWidth || img.width;
      const height = img.naturalHeight || img.height;

      canvas.width = width;
      canvas.height = height;

      const lineStrokeWidth = Math.max(2,Math.floor(Math.max(width,height)/300))
      const cornerMarkerRadius = Math.max(5,Math.floor(Math.max(width,height)/200))

      ctx.clearRect(0, 0, width, height);
      ctx.drawImage(img, 0, 0, width, height);

      // Draw selection overlay
      if (selection) {
        ctx.save();
        ctx.fillStyle = "rgba(0,0,0,0.35)";
        ctx.beginPath();
        ctx.rect(0, 0, width, height);
        ctx.rect(selection.x, selection.y, selection.w, selection.h);
        ctx.fill("evenodd");

        ctx.strokeStyle = "red";
        ctx.lineWidth = lineStrokeWidth;
        ctx.setLineDash([6, 4]);
        ctx.strokeRect(selection.x, selection.y, selection.w, selection.h);
        ctx.restore();
      }

      // Draw quality-colored circles
      if (coordsEnd && coordsOrigin && measuredDistance) {
        // IMPORTANT: Capture clean image data BEFORE drawing any overlays
        // This prevents corner markers and other overlays from contaminating quality calculations
        // Only capture when we actually need it for quality checking (huge performance improvement)
        const cleanImageData = ctx.getImageData(0, 0, width, height);
        const offsetX = selectionOld ? selectionOld.x : 0;
        const offsetY = selectionOld ? selectionOld.y : 0;
        const diameter = measuredDistance;
        const gapX = (Math.abs(coordsOrigin.x-coordsEnd.x)-(columnCount-1)*diameter)/(columnCount-1);
        const gapY = (Math.abs(coordsOrigin.y-coordsEnd.y)-(rowCount-1)*diameter)/(rowCount-1);
        ctx.lineWidth = lineStrokeWidth;

        // Initialize CV data storage for hover tooltips
        const cvData = [];

        for (let indexColumn = 0; indexColumn < columnCount; indexColumn++) {
          for (let indexRow = 0; indexRow < rowCount; indexRow++) {
            let coordX = coordsOrigin.x + diameter*indexColumn + gapX*indexColumn - offsetX;
            let coordY = coordsOrigin.y + diameter*indexRow + gapY*indexRow - offsetY;

            // Conditionally calculate and display quality checking
            if (showQualityCheck) {
              // Calculate quality for the inner 50% of the circle (radius = 0.25 * diameter)
              const innerRadius = diameter * 0.25;
              const pixels = getCirclePixels(cleanImageData, coordX, coordY, innerRadius);
              const cv = calculateGreenBlueCV(pixels);
              const fillColor = getCVColor(cv);

              // Store CV data for hover tooltips
              cvData.push({
                row: indexRow,
                col: indexColumn,
                cv: cv,
                x: coordX,
                y: coordY,
                radius: diameter / 2
              });

              // Fill circle with quality color
              ctx.fillStyle = fillColor;
              ctx.beginPath();
              ctx.arc(coordX, coordY, diameter/2, 0, Math.PI*2);
              ctx.fill();
            }

            // Always draw circle outline
            ctx.beginPath();
            ctx.arc(coordX, coordY, diameter/2, 0, Math.PI*2);
            ctx.stroke();

          }
        }

        // Store CV data in ref for hover detection
        cvDataRef.current = showQualityCheck ? cvData : null;
      }

      // Draw corner markers AFTER quality circles (drawn on top)
      if (coordsOrigin) {
        ctx.beginPath();
        ctx.fillStyle = "green";
        if (!selectionOld) ctx.arc(coordsOrigin.x, coordsOrigin.y, cornerMarkerRadius, 0, Math.PI * 2);
        else ctx.arc(coordsOrigin.x-selectionOld.x, coordsOrigin.y-selectionOld.y, cornerMarkerRadius, 0, Math.PI * 2);
        ctx.fill();
      }
      if (coordsEnd) {
        ctx.beginPath();
        ctx.fillStyle = "red";
        if (!selectionOld) ctx.arc(coordsEnd.x, coordsEnd.y, cornerMarkerRadius, 0, Math.PI * 2);
        else ctx.arc(coordsEnd.x-selectionOld.x, coordsEnd.y-selectionOld.y, cornerMarkerRadius, 0, Math.PI * 2);
        ctx.fill();
      }

      // Draw size line AFTER everything else
      if (sizeLine) {
        ctx.beginPath();
        ctx.strokeStyle = "red";
        ctx.lineWidth = lineStrokeWidth;
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
    }, [displayedImage, selection, coordsOrigin, coordsEnd, loadImage, sizeLine, rowCount, columnCount, selectionOld, measuredDistance, showQualityCheck]);
    
    useEffect(() => {
      if (displayedImage) {
        draw();
      }
    }, [draw, displayedImage, selection, coordsOrigin, coordsEnd, sizeLine, rowCount, columnCount]);
    
  
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
    const onPointerLeave = () => {
      setIsDrawing(false);
      setHoveredWell(null);
    };

    // Handle hover to show CV tooltip
    const onCanvasHover = (e) => {
      // Only process hover if quality check is enabled and we have CV data
      if (!showQualityCheck || !cvDataRef.current || uploadStage !== 'parameters') {
        setHoveredWell(null);
        return;
      }

      const { x, y } = getPointerPos(e, canvasRef);

      // Check if mouse is over any well
      for (const wellData of cvDataRef.current) {
        const dx = x - wellData.x;
        const dy = y - wellData.y;
        const distance = Math.sqrt(dx * dx + dy * dy);

        if (distance <= wellData.radius) {
          setHoveredWell(wellData);
          return;
        }
      }

      setHoveredWell(null);
    };
  
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
        const offsetX = 0;
        const offsetY = 0;
        const diameter = measuredDistance;
        const gapX = (Math.abs(coordsOrigin.x-coordsEnd.x)-(columnCount-1)*diameter)/(columnCount-1);
        const gapY = (Math.abs(coordsOrigin.y-coordsEnd.y)-(rowCount-1)*diameter)/(rowCount-1);
        for (let indexColumn = 0; indexColumn < columnCount; indexColumn++) {
          for (let indexRow = 0; indexRow < rowCount; indexRow++) {
            let coordX = coordsOrigin.x + diameter*indexColumn + gapX*indexColumn - offsetX;
            let coordY = coordsOrigin.y + diameter*indexRow + gapY*indexRow - offsetY;
            setWellCenters(prev => ([...prev, {'x':coordX, 'y':coordY, 'indexColumn' : indexColumn, 'indexRow' : indexRow}]));
          }
        }
        reset();
        setUploadStage('wellSelection');
      }
    }
    
    const handleInputContainerClick = (e) => {
      if (name && description) {
        fileInputRef.current.click();
      }
      else {
        showNotification('Name or Description is missing')
      }
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
        <form action="" className="flex flex-col gap-4 items-center mb-4">
        <div className="text-base">
          <label className='text-left' htmlFor="name">Project Name</label>
          <input type="text" id="name" className="inpt !w-full max-w-100 " onChange={(e) => setName(e.target.value)}/>
        </div>
        <div className="text-base">
          <label htmlFor="desc">Project Description</label>
          <textarea type="s" id="desc" className="inpt !w-full max-w-100 h-20" onChange={(e) => setDescription(e.target.value)}/>
        </div>
        </form>
        <button onClick={handleInputContainerClick} className='cursor-pointer w-[min(90vw,50rem)] h-[30vh] bg-igem-gray
          rounded-xl flex justify-center items-center text-base'>
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
      </>
    )}

      {image && (uploadStage === 'parameters' || uploadStage === 'crop') && (
        <div className="mb-6">
          <div className="p-2 bg-igem-gray rounded-xl flex flex-col relative">
            <canvas
              ref={canvasRef}
              onPointerDown={onPointerDown}
              onPointerMove={(e) => {
                onPointerMove(e);
                if (uploadStage === 'parameters') {
                  onCanvasHover(e);
                }
              }}
              onPointerUp={onPointerUp}
              onPointerLeave={onPointerLeave}
              className="border rounded-2xl shadow w-auto h-full touch-none"
              onClick={(e) => handleClick(e, canvasRef)}
            />
            {uploadStage === 'parameters' && (
              <p className="mt-4 !text-igem-black text-lg font-mono">
                Clicked at: ({coords.x}, {coords.y})
                {hoveredWell && showQualityCheck && (
                  <span className="ml-4 text-white bg-black/80 px-3 py-1 rounded">
                    CV: {hoveredWell.cv !== null ? hoveredWell.cv.toFixed(4) : 'N/A'}
                  </span>
                )}
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
        setSelection={setSelection}
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
          showQualityCheck={showQualityCheck}
          setShowQualityCheck={setShowQualityCheck}
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