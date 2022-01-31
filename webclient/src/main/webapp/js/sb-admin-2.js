(function(jQuery) {
  "use strict"; // Start of use strict

  // Toggle the side navigation
  jQuery("#sidebarToggle, #sidebarToggleTop").on('click', function(e) {
    jQuery("body").toggleClass("sidebar-toggled");
    jQuery(".sidebar").toggleClass("toggled");
    if (jQuery(".sidebar").hasClass("toggled")) {
      // jQuery('.sidebar .collapse').collapse('hide');
    };
  });

  // Close any open menu accordions when window is resized below 768px
  jQuery(window).resize(function() {
    if (jQuery(window).width() < 768) {
      jQuery('.sidebar .collapse').collapse('hide');
    };
    
    // Toggle the side navigation when window is resized below 480px
    if (jQuery(window).width() < 480 && !jQuery(".sidebar").hasClass("toggled")) {
      jQuery("body").addClass("sidebar-toggled");
      jQuery(".sidebar").addClass("toggled");
      jQuery('.sidebar .collapse').collapse('hide');
    };
  });

  // Prevent the content wrapper from scrolling when the fixed side navigation hovered over
 jQuery('body.fixed-nav .sidebar').on('mousewheel DOMMouseScroll wheel', function(e) {
    if (jQuery(window).width() > 768) {
      var e0 = e.originalEvent,
        delta = e0.wheelDelta || -e0.detail;
      this.scrollTop += (delta < 0 ? 1 : -1) * 30;
      e.preventDefault();
    }
  });

  // Scroll to top button appear
  jQuery(document).on('scroll', function() {
    var scrollDistance = jQuery(this).scrollTop();
    if (scrollDistance > 100) {
      jQuery('.scroll-to-top').fadeIn();
    } else {
      jQuery('.scroll-to-top').fadeOut();
    }
  });

  // Smooth scrolling using jQuery easing
  jQuery(document).on('click', 'a.scroll-to-top', function(e) {
    var anchor = jQuery(this);
    jQuery('html, body').stop().animate({
      scrollTop: (jQuery(jQuery(anchor).attr('href')).offset().top)
    }, 1000, 'easeInOutExpo');
    e.preventDefault();
  });

})(jQuery); // End of use strict
