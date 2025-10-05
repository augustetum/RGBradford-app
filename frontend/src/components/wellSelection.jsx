import React, { useState, useRef, useEffect, useCallback } from "react";
import { AnimatePresence, motion } from 'framer-motion';

function WellSelection({ setWellCenters, originalImage, measuredDistance, wellCenters, loadImage, setUploadStage}) {
    const canvasRef = useRef()
    const [displayedImage, setDisplayedImage] = useState(originalImage);
    const [wellType, setWellType] = useState('SAMPLE');
    const [wells, setWells] = useState(wellCenters.map((center) => ({...center, type : 'EMPTY'})));
    const [isDrawing, setIsDrawing] = useState(false); 

    function submit() {
      if (wells.filter(obj => obj.type === 'STANDARD').length && wells.filter(obj => obj.type === 'SAMPLE').length) {
        setWellCenters(wells)
        setUploadStage('calibration')

      } 
      else {
        alert("Select at least 1 Sample and 1 Calibration well")
      }
    }

    function handleClick(e, canvasRef) {
      const canvas = canvasRef.current;
      if (!canvas) return { x: 0, y: 0 };
      const rect = canvas.getBoundingClientRect();
      const scaleX = canvas.width / rect.width;
      const scaleY = canvas.height / rect.height;
      const x = (e.clientX - rect.left) * scaleX;
      const y = (e.clientY - rect.top) * scaleY;
      return { x, y };
    }

    function handleToggle(e) {
      let clickCoords = handleClick(e, canvasRef)
      let dist = 0
      wellCenters.forEach(center => {
        dist = Math.sqrt((center.x-clickCoords.x)**2 + (center.y-clickCoords.y)**2)
        if (dist < measuredDistance/2) {
          setWells(prev =>
            prev.map(obj =>
              obj.x === center.x && obj.y === center.y
                ? { ...obj, type: wellType }
                : obj
            )
          )
          return 0; 
        }
      });
    }
    
    const draw = useCallback(async () => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        const colorDict = {'SAMPLE' : 'rgba(173, 70, 255, 0.5)', 'STANDARD' : 'rgba(255, 105, 0, 0.5)', 'EMPTY' : 'rgba(0,0,0,0)'}
        const img = await loadImage(displayedImage);
        const width = img.naturalWidth || img.width;
        const height = img.naturalHeight || img.height;
        canvas.width = width;
        canvas.height = height;
        ctx.clearRect(0, 0, width, height);
        ctx.drawImage(img, 0, 0, width, height);
        wells.forEach(well => {
          ctx.beginPath();
          ctx.arc(well.x,well.y,measuredDistance/2,0,Math.PI*2)
          ctx.fillStyle = colorDict[well.type]
          ctx.stroke()
          ctx.fill()
        });
      }, [displayedImage, loadImage, wells ]);
      
      useEffect(() => {
        if (displayedImage) {
          draw();
        }
      }, [draw, displayedImage, wells]);

  return (
    <AnimatePresence mode="wait">
    <motion.div
     key={displayedImage}
     initial={{ opacity: 0, y: 20 }}
     animate={{ opacity: 1, y: 0 }}
     exit={{ opacity: 0, y: -20 }}
     transition={{ duration: 0.2 }}
     onAnimationComplete={() => {
      if (canvasRef.current && displayedImage) {
        draw();
      }}}>
        <div className=" p-2 bg-igem-gray
          rounded-xl flex flex-col text-base">
          
        <canvas
          ref={canvasRef}
          className="border rounded-2xl shadow w-auto h-full touch-none"
          onPointerDown={() => setIsDrawing(true)}
          onPointerMove={(e) => {if (isDrawing) {handleToggle(e)}}}
          onPointerUp={() => setIsDrawing(false)}
          onClick={handleToggle}
        />
        </div>
        <div className="w-[min(90vw,50rem)] text-base">
          <div className="flex gap-4 justify-center mt-6 flex-wrap">
            <button onClick={() => setWellType('EMPTY')} className={`${wellType == 'EMPTY' ? '!bg-red-500' : ""} btn text-base`}>
              Clear Well
            </button>
            <button onClick={() => setWellType('STANDARD')} className={`${wellType == 'STANDARD' ? '!bg-orange-500' : ""} btn text-base`}>
              Select Calibration
            </button>
            <button onClick={() => setWellType('SAMPLE')} className={`${wellType == 'SAMPLE' ? '!bg-purple-500' : ""} btn text-base`}>
              Select Sample
            </button>
            <button onClick={submit} className={`font-bold !bg-green-500 btn text-base`}>
              Next
            </button>
          </div>
        </div>
    </motion.div>
    </AnimatePresence>
  )
}

export default WellSelection