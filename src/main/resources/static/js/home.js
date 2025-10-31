// Home Page Carousel JS
document.addEventListener('DOMContentLoaded', () => {
  const carousel = document.querySelector('#heroCarousel');
  
  // Initialize Bootstrap carousel with custom options
  const carouselInstance = new bootstrap.Carousel(carousel, {
    interval: 10000,  // Slide every 10 seconds
    ride: 'carousel',
    wrap: true,       // Infinite loop
    pause: false      // Keep sliding even on hover
  });

  console.log("✅ Hero carousel initialized successfully.");
});
