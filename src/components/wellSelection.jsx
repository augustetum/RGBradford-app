
import React, { useState, useRef, useEffect, useCallback } from "react";
import { AnimatePresence, motion } from 'framer-motion';
function WellSelection({originalImage, measuredDistance, wellCenters, handleClick, loadImage}) {
    const canvasRef = useRef()
    const [displayedImage, setDisplayedImage] = useState(originalImage);
    const [wellType, setWellType] = useState('empty');
    const [wells, setWells] = useState({'empty' : [], 'sample' : [],'calibration' : [],});

    function handleToggle(e) {
      const clickCoords = handleClick()
      console.log(clickCoords)
      let dist = 0
      wellCenters.forEach(center => {
        dist = Math.sqrt((center.x-clickCoords.x)**2 + (center.y-clickCoords.y)**2)
        if (dist < measuredDistance/2) {
          setWells(prev => ({...prev, wellType: [...prev.wellType, center] }))
          return 0;
        }
      });
    }
    
    const draw = useCallback(async () => {
        const canvas = canvasRef.current;
        if (!canvas) return;
        const ctx = canvas.getContext("2d");
        const colorDict = {'empty' : 'red', 'sample' : 'blue', 'calibration' : 'green'}
        const img = await loadImage(displayedImage);
        const width = img.naturalWidth || img.width;
        const height = img.naturalHeight || img.height;
      
        canvas.width = width;
        canvas.height = height;
      
        ctx.clearRect(0, 0, width, height);
        ctx.drawImage(img, 0, 0, width, height);
  
        wellCenters.forEach(center => {
          ctx.beginPath();
          ctx.arc(center.x,center.y,measuredDistance/2,0,Math.PI*2)
          ctx.stroke()
        });

        ['empty','sample','calibration'].forEach(typeofwell => {
          wells.typeofwell.forEach(well => {
            ctx.beginPath();
            ctx.arc(well.x,well.y,measuredDistance/2,0,Math.PI*2)
            ctx.fill(colorDict.typeofwell)
          });
        });

      }, [displayedImage, loadImage, ]);
      
      useEffect(() => {
        if (displayedImage) {
          draw();
        }
      }, [draw, displayedImage]);

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
          onClick={handleToggle}
        />
        </div>
    </motion.div>
    </AnimatePresence>
  )
}

export default WellSelection