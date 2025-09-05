
import React, { useState, useRef, useEffect, useCallback } from "react";
import { AnimatePresence, motion } from 'framer-motion';
import { canvas } from "motion/react-client";
function WellSelection({setWellCenters, originalImage, measuredDistance, wellCenters, handleClick, loadImage, setUploadStage}) {
    const canvasRef = useRef()
    const [displayedImage, setDisplayedImage] = useState(originalImage);
    const [wellType, setWellType] = useState('empty');
    const [wells, setWells] = useState(wellCenters.map((center) => ({...center, type : 'empty'})));
    const [isDrawing, setIsDrawing] = useState(false); 

    function submit() {
      setWellCenters(wells)
      setUploadStage('calibration')
    }

    function handleToggle(e) {
      const clickCoords = handleClick(e, canvasRef)
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
          );
          return 0; 
        }
      });
    }
    
    const draw = useCallback(async () => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        const colorDict = {'sample' : 'rgba(173, 70, 255, 0.5)', 'calibration' : 'rgba(255, 105, 0, 0.5)', 'empty' : 'rgba(0,0,0,0)'}
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
          rounded-xl flex flex-col">
          
        <canvas
          ref={canvasRef}
          className="border rounded-2xl shadow w-auto h-full touch-none"
          onPointerDown={() => setIsDrawing(true)}
          onPointerMove={(e) => {if (isDrawing) {handleToggle(e)}}}
          onPointerUp={() => setIsDrawing(false)}
          onClick={handleToggle}
        />
        </div>
        <div className="w-[min(90vw,50rem)]">
          <div className="flex gap-4 justify-center mt-6 flex-wrap">
            <button onClick={() => setWellType('empty')} className={`${wellType == 'empty' ? '!bg-red-500' : ""} btn`}>
              Select Empty
            </button>
            <button onClick={() => setWellType('calibration')} className={`${wellType == 'calibration' ? '!bg-orange-500' : ""} btn`}>
              Select Calibration
            </button>
            <button onClick={() => setWellType('sample')} className={`${wellType == 'sample' ? '!bg-purple-500' : ""} btn`}>
              Select Sample
            </button>
            <button onClick={submit} className={`!bg-green-500 btn`}>
              Submit
            </button>
          </div>
        </div>
    </motion.div>
    </AnimatePresence>
  )
}

export default WellSelection