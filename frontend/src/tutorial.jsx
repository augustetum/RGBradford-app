const tutorial = {
    crop: (
      <p className="!text-white text-2xl text-justify mb-4">
        To crop your image before processing, click "Enter Crop Mode", then drag a rectangle to your desired size, making sure that the whole 
        plate is visible, and finally click crop. If you want to undo your crop, click "Reset To Original".
      </p>
    ),
    parameters: (
      <p className="!text-white text-2xl text-justify mb-4">
        First, click on the <b>center</b> of the top-left (A1) well, then click "Set Top Left Well".
        Then, do the same thing for the bottom-right well. Finally, click "Enter Diameter Select Mode", and drag across a single well to select its diameter. If this is proving difficult to do precisely, you can use the Zoom buttons, same as the crop buttons from before. After clicking "Exit Diameter Select Mode", a grid of wells should show up on the image. 
        Make sure they align well with the actual grid of wells. The colors of the wells indicate homogeneity - green wells are homogeneous, red are substantially non-homogeneous. If the CV (which can be seen upon hover), exceeds 0.3 for some imporant wells even upon slight adjustment of parameters, a retake of the photo should be considered.
      </p>
    ),
    wellSelection: (
      <p className="!text-white text-2xl text-justify mb-4">
        Select all of the calibration (known concentration) and sample wells by clicking/dragging, you can change the selection mode by clicking the respective buttons. "Clear Well" lets you clear wells (set them to empty).
      </p>
    ),
    calibration: (
      <p className="!text-white text-2xl text-center mb-4">
        Enter the concentrations for the calibration wells.
      </p>
    )
  };
  
  export default tutorial;
  